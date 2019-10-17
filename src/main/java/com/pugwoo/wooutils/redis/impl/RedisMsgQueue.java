package com.pugwoo.wooutils.redis.impl;

import com.pugwoo.wooutils.redis.RedisHelper;
import com.pugwoo.wooutils.redis.RedisMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于redis实现的带ack机制的消息队列
 * @author nick
 */
public class RedisMsgQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMsgQueue.class);

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
        // TODO
        return null;
    }

    /**
     * 接收消息，永久阻塞式，使用默认的actTimeout值
     * @param redisHelper
     * @param topic 即redis的key
     * @return
     */
    public static RedisMsg receive(RedisHelper redisHelper, String topic) {
        // TODO
        return null;
    }

    /**
     * 接收消息
     * @param redisHelper
     * @param topic 即redis的key
     * @param waitTimeoutSec 指定接口阻塞等待时间，0表示不阻塞，-1表示永久等待，大于0为等待的秒数
     * @param ackTimeoutSec ack确认超时的秒数
     * @return 如果没有接收到消息，返回null
     */
    public static RedisMsg receive(RedisHelper redisHelper, String topic, int waitTimeoutSec, int ackTimeoutSec) {
        // TODO
        return null;
    }

    /**
     * 确认消费消息
     * @param redisHelper
     * @param topic 即redis的key
     * @param msgUuid
     * @return
     */
    public static boolean ack(RedisHelper redisHelper, String topic, String msgUuid) {
        // TODO
        return false;
    }

}
