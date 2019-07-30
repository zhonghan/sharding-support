package com.karl.framework.sharding.plugin.config;

import com.karl.framework.sharding.strategy.ShardStrategy;

import java.util.Set;

/**
 * @author karl.zhong
 */
public class TableShardStrategyConfig {
    private Set<String> tableNames;
    private ShardStrategy strategy;

    public TableShardStrategyConfig(Set<String> tableNames, ShardStrategy strategy) {
        this.tableNames = tableNames;
        this.strategy = strategy;
    }

    public Set<String> getTableName() {
        return tableNames;
    }

    public ShardStrategy getStrategy() {
        return strategy;
    }
}