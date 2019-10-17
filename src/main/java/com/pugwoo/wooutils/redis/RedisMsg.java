package com.pugwoo.wooutils.redis;

/**
 * redis消息队列的消息体
 */
public class RedisMsg {

    /**消息uuid*/
    private String uuid;

    /**消息正文*/
    private String msg;

    /**发送消息时间戳，毫秒*/
    private long sendTime;

    /**消费时间戳，毫秒；未被消费时，其值为null*/
    private Long recvTime;

    /**ack确认超时时间，秒*/
    private int ackTimeout;

}
