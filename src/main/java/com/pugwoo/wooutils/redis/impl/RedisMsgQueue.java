package com.pugwoo.wooutils.redis.impl;

import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.redis.RedisHelper;
import com.pugwoo.wooutils.redis.RedisMsg;
import com.pugwoo.wooutils.string.Hash;
import com.pugwoo.wooutils.string.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

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

            if(StringTools.isEmpty(uuid)) {
                return null;
            }

            String msgJson = jedis.hget(mapKey, uuid);
            if(StringTools.isEmpty(msgJson)) {
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


    // TODO 还有2个定时任务任务，在Redishelperimpl中做成线程，再过来调这里

}
