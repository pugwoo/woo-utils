package com.pugwoo.wooutils.redis;

public class NoJedisConnectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoJedisConnectionException() {
    }

    public NoJedisConnectionException(String errmsg) {
        super(errmsg);
    }

    public NoJedisConnectionException(Throwable e) {
        super(e);
    }

}
