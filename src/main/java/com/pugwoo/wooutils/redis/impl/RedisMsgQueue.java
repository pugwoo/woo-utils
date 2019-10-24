package com.pugwoo.wooutils.redis.impl;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.redis.RedisHelper;
import com.pugwoo.wooutils.redis.RedisMsg;
import com.pugwoo.wooutils.string.Hash;
import com.pugwoo.wooutils.string.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于redis实现的带ack机制的消息队列
 * @author nick
 */
public class RedisMsgQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMsgQueue.class);

    private static String getPendingKey(String topic) {
        return topic + ":" + "MQLIST";
    }

    private static String getDoingKey(String topic) {
        return topic + ":" + "MQDOING";
    }

    private static String getMapKey(String topic) {
        return topic + ":" + "MQMSG";
    }

    /**
     * 发送消息，返回消息的uuid。默认的超时时间是30秒
     * @param redisHelper
     * @param topic topic将是redis的key
     * @param msg
     * @return
     */
    public static String send(RedisHelper redisHelper, String topic, String msg) {
        return send(redisHelper, topic, msg, 30);
    }

    /**
     * 发送消息，返回消息的uuid
     * @param redisHelper
     * @param topic 即redis的key
     * @param msg
     * @param defaultAckTimeoutSec 默认ack超时时间：当消费者消费了消息却没来得及设置ack超时时间时的默认超时秒数。建议处理时间默认比较长的应用，可以将该值设置较大，例如60秒或120秒
     * @return 消息的uuid，发送失败返回null
     */
    public static String send(RedisHelper redisHelper, String topic, String msg, int defaultAckTimeoutSec) {

        String uuid = "rmq" + Hash.md5(UUID.randomUUID().toString());

        RedisMsg redisMsg = new RedisMsg();
        redisMsg.setUuid(uuid);
        redisMsg.setMsg(msg);
        redisMsg.setSendTime(System.currentTimeMillis());
        redisMsg.setAckTimeout(defaultAckTimeoutSec);

        String listKey = getPendingKey(topic);
        String mapKey = getMapKey(topic);

        List<Object> result = redisHelper.executePipeline(pipeline -> {
            pipeline.hset(mapKey, uuid, JSON.toJson(redisMsg));
            pipeline.lpush(listKey, uuid);
        });

        boolean success = true;
        if(result != null && result.size() == 2) {
            if(!(result.get(0)!=null && (result.get(0) instanceof Long) && result.get(0).equals(1L))) {
                success = false;
                LOGGER.error("send msg:{}, content:{} fail, redis result[0] != 1", uuid, msg);
            }
            if(!(result.get(0)!=null && (result.get(0) instanceof Long) && ((Long)result.get(0)) > 0L)) {
                success = false;
                LOGGER.error("send msg:{}, content:{} fail, redis result[1] < 1", uuid, msg);
            }
        } else {
            LOGGER.error("send msg:{}, content:{} fail, redis result size != 2",
                    uuid, msg);
            success = false;
        }

        return success ? uuid : null;
    }

    /**
     * 接收消息，永久阻塞式，使用默认的actTimeout值
     * @param redisHelper
     * @param topic 即redis的key
     * @return
     */
    public static RedisMsg receive(RedisHelper redisHelper, String topic) {
        return receive(redisHelper, topic, -1, null);
    }

    /**
     * 接收消息
     * @param redisHelper
     * @param topic 即redis的key
     * @param waitTimeoutSec 指定接口阻塞等待时间，0表示不阻塞，-1表示永久等待，大于0为等待的秒数
     * @param ackTimeoutSec ack确认超时的秒数，设置为null则表示不修改，使用发送方设置的默认超时值
     * @return 如果没有接收到消息，返回null
     */
    public static RedisMsg receive(RedisHelper redisHelper, String topic, int waitTimeoutSec, Integer ackTimeoutSec) {

        String listKey = getPendingKey(topic);
        String doingKey = getDoingKey(topic);
        String mapKey = getMapKey(topic);

        RedisMsg redisMsg = redisHelper.execute(jedis -> {
            String uuid = null;
            if(waitTimeoutSec == 0) {
                uuid = jedis.rpoplpush(listKey, doingKey);
            } else {
                uuid = jedis.brpoplpush(listKey, doingKey, waitTimeoutSec < 0 ? 0 : waitTimeoutSec);
            }

            if(uuid == null) { // 没有收到消息，属于正常情况
                return null;
            }

            String msgJson = jedis.hget(mapKey, uuid);
            if(StringTools.isEmpty(msgJson)) {
                LOGGER.error("get uuid:{} msg fail, msg is empty", uuid);
                return null;
            }

            RedisMsg _redisMsg = JSON.parse(msgJson, RedisMsg.class);
            _redisMsg.setRecvTime(System.currentTimeMillis());
            if(ackTimeoutSec != null) {
                _redisMsg.setAckTimeout(ackTimeoutSec);
            }
            jedis.hset(mapKey, uuid, JSON.toJson(_redisMsg));

            return _redisMsg;
        });

        return redisMsg;
    }

    /**
     * 确认消费消息
     * @param redisHelper
     * @param topic 即redis的key
     * @param msgUuid
     * @return
     */
    public static boolean ack(RedisHelper redisHelper, String topic, String msgUuid) {
        String doingKey = getDoingKey(topic);
        String mapKey = getMapKey(topic);

        redisHelper.executePipeline(pipeline -> {
            pipeline.lrem(doingKey, 0, msgUuid);
            pipeline.hdel(mapKey, msgUuid);
        });

        return true;
    }

    ////////////// 以下是清理任务相关的

    /**获得消息体，如果不存在返回null*/
    private static RedisMsg getMsg(RedisHelper redisHelper, String topic, String uuid) {
        String mapKey = getMapKey(topic);

        String json = redisHelper.execute(jedis -> jedis.hget(mapKey, uuid));
        if(StringTools.isEmpty(json)) {
            return null;
        }
        RedisMsg redisMsg = JSON.parse(json, RedisMsg.class);
        return redisMsg;
    }

    /**查询超时的消息，里面包含了消费时间为null的消息，外层清理时需要10秒延迟清理*/
    private static List<RedisMsg> getExpireDoingMsg(RedisHelper redisHelper, String topic) {

        String doingKey = getDoingKey(topic);
        String mapKey = getMapKey(topic);

        List<RedisMsg> expireMsg  = redisHelper.execute(jedis -> {
            List<String> uuidList = jedis.lrange(doingKey, 0, -1);

            List<RedisMsg> _expireMsg = new ArrayList<>();

            for(String uuid : uuidList) {
                String json = jedis.hget(mapKey, uuid);
                if(StringTools.isEmpty(json)) {
                    continue;
                }
                RedisMsg redisMsg = JSON.parse(json, RedisMsg.class);
                long now = System.currentTimeMillis();
                if(redisMsg.getRecvTime() == null || redisMsg.getRecvTime() + redisMsg.getAckTimeout() * 1000 < now) {
                    _expireMsg.add(redisMsg);
                }
            }

            return _expireMsg;
        });

        return expireMsg;
    }

    /**复原消费超时的消息*/
    private static void recoverMsg(RedisHelper redisHelper, String topic, String uuid) {

        String listKey = getPendingKey(topic);
        String doingKey = getDoingKey(topic);

        redisHelper.execute(jedis -> {
            // 这里如果是先加后删，则比较大概率被等待receive的客户端拿到之后pop push回doing列表，此动作如果在删除之前进行，就会出现误删情况
            // 如果是先删后加，理论上不会有问题，除了极端情况下，redis执行了第一条命令之后挂了才可能导致丢失数据，但这种可能性已经远远比第一种低
            return jedis.eval("redis.call('LREM', KEYS[1], 0, ARGV[1]); redis.call('LPUSH', KEYS[2], ARGV[1])",
                    ListUtils.newArrayList(doingKey, listKey), ListUtils.newArrayList(uuid));
        });
    }

    /**清理消息队列中，uuid已经不存在了，但是map中还在的消息，每天清理一次即可（这类消息是低概率且只有可能在发送环节出现），要根据发送时间延迟清理*/
    // public static void clearMap(RedisHelper redisHelper, String topic) {
    // 暂不实现该方法
    //}

    // 恢复超时消息，每10秒跑一次
    public static class RecoverMsgTask extends Thread {

        private RedisHelper redisHelper;
        private Map<String, String> topics = new ConcurrentHashMap<>(); // 存放需要更新的主题

        public RecoverMsgTask(RedisHelper redisHelper) {
            this.redisHelper = redisHelper;
        }

        public void addTopic(String topic) {
            if(topics.containsKey(topic)) {
                return;
            }
            topics.put(topic, "");
        }

        /**清理过期消息，返回true表示有消费时间为null的情况，已经睡眠了10秒去清理了；返回false则表示没有*/
        private boolean doClean() {
            Map<String, List<String>> waitToClear = new HashMap<>(); // 等待清理的topic -> 消息uuid列表

            for(String topic : topics.keySet()) {
                List<RedisMsg> expires = getExpireDoingMsg(redisHelper, topic);
                if(expires.isEmpty()) {
                    continue; // 不需要处理
                }

                LOGGER.warn("expire topic:{} msg count:{}", topic, expires.size());
                List<String> nullRecvTimeList = new ArrayList<>();

                for(RedisMsg redisMsg : expires) {
                    if(redisMsg.getRecvTime() == null) {
                        nullRecvTimeList.add(redisMsg.getUuid());
                    } else {
                        recoverMsg(redisHelper, topic, redisMsg.getUuid());
                    }
                }

                if(!nullRecvTimeList.isEmpty()) {
                    LOGGER.warn("expire topic:{} msg with null recvTime count:{}, msg uuids:{}",
                            topic, nullRecvTimeList.size(), JSON.toJson(nullRecvTimeList));
                    waitToClear.put(topic, nullRecvTimeList);
                }
            }

            if(!waitToClear.isEmpty()) { // 清理，需要再检查一遍是否消费时间确实为null
                try {
                    Thread.sleep(10000); // 这个10秒钟还关乎消费时间为null的消息的延迟处理
                } catch (InterruptedException e) { // ignore
                }

                for(Map.Entry<String, List<String>> entry : waitToClear.entrySet()) {
                    for(String uuid : entry.getValue()) {
                        RedisMsg msg = getMsg(redisHelper, entry.getKey(), uuid);
                        if(msg != null && msg.getRecvTime() == null) {
                            recoverMsg(redisHelper, entry.getKey(), uuid);
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        private static final String LOCK_KEY = "_RedisMsgQueueRecoverMsgTaskLock_";

        @Override
        public void run() {

            // 一直尝试拿锁
            String lockUuid;
            while(true) {
                lockUuid = redisHelper.requireLock(LOCK_KEY, "-", 30);
                if(lockUuid == null) { // 没有拿到锁，等待30秒重试
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) { // ignore
                    }
                } else {
                    break;
                }
            }

            // 起一条心跳线程一直延期,只要拿到锁，就一直用不释放
            final String _lockUuid = lockUuid;
            Thread thread = new Thread(() -> {
                while(true) { // 一直循环不会退出，每9秒续30秒，也即一共有3次续期机会
                    redisHelper.renewalLock(LOCK_KEY, "-", _lockUuid,30);

                    try {
                        Thread.sleep(9000);
                    } catch (InterruptedException e) { // ignore
                    }
                }
            });
            thread.setName("RedisMsgQueue.RecoverMsgRenewalLockTask");
            thread.start();

            while(true) { // 一直循环不会退出
                try {
                    boolean hasSleep = doClean();
                    if(!hasSleep) { // 如果刚清理完，还没睡眠，则
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) { // ignore
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("do clean task fail", e);
                }
            }
        }
    }

}
