package com.karl.framework.sharding.exception;

/**
 * @author karl.zhong
 */
public class ExecuteShardingSqlException extends RuntimeException {

    public ExecuteShardingSqlException(String message, Exception e) {
        super(message, e);
    }
}
