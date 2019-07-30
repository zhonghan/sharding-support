package com.karl.framework.sharding.strategy.ma.annotation;

public enum QueryType {
    /**
     * count查询，查询数量
     */
    COUNT,
    /**
     * 通过PageHelper插件分页查询
     */
    PAGE_HELPER,
    /**
     * 正常的查询， 非count，也非PageHelper
     */
    DEFAULT;
}
