package com.karl.framework.sharding.plugin.config;


import java.util.List;

/**
 * @author karl.zhong
 */
public class ShardPluginConfiguration {

    private List<TableShardStrategyConfig> tableShardStrategyList;

    public List<TableShardStrategyConfig> getTableShardStrategyList() {
        return tableShardStrategyList;
    }

    public ShardPluginConfiguration setTableShardStrategyList(List<TableShardStrategyConfig> tableShardStrategyList) {
        this.tableShardStrategyList = tableShardStrategyList;
        return this;
    }
}
