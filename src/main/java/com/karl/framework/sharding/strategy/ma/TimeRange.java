package com.karl.framework.sharding.strategy.ma;

/**
 * @author karl.zhong
 */
public class TimeRange {
    private TimeWrapper startTime;
    private TimeWrapper endTime;
    private String tableSuffix;
    public TimeRange(TimeWrapper startTime, TimeWrapper endTime, String tableSuffix) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.tableSuffix = tableSuffix;
    }

    public TimeWrapper getStartTime() {
        return startTime;
    }

    public TimeWrapper getEndTime() {
        return endTime;
    }

    public String getTableSuffix() {
        return tableSuffix;
    }

    @Override
    public String toString() {
        return "TimeRange{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
