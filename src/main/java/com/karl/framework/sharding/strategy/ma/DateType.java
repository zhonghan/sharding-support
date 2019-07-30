package com.karl.framework.sharding.strategy.ma;

/**
 * @author karl.zhong
 */

public enum DateType {
    /**
     * java.util.Date
     */
    JAVA_UTIL_DATE,
    /**
     * yyyy-MM-dd
     */
    DATE_STRING,
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    DATE_TIME_STRING;
}
