package com.pugwoo.wooutils.redis;

/**
 * redis消息队列的状态
 */
public class RedisQueueStatus {

    /**
     * 消息队列中当前等待消费的消息数
     */
    private int pendingCount;

    /**
     * 消息队列中正在消费的消息数
     */
    private int doingCount;

    /**
     * 最后一个自然天的消费发送总数 （暂不实现）
     */
    // private int daySendMsgCount;

    /**
     * 最近一个自然天的消息消费超时数 （暂不实现）
     */
    // private int dayExpireMsgCount;


    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getDoingCount() {
        return doingCount;
    }

    public void setDoingCount(int doingCount) {
        this.doingCount = doingCount;
    }

}
