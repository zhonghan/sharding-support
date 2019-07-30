package com.karl.framework.sharding.strategy.ma.holder;

import com.karl.framework.sharding.strategy.ma.TimeRange;
import com.karl.framework.sharding.strategy.ma.annotation.TimeShardQuery;

import java.util.List;

/**
 * @author karl.zhong
 */
public class TimeShardingContextHolder {
    private static ThreadLocal<TimeRangeQueryContext> threadLocal = new ThreadLocal<>();

    public static boolean exist() {
        return threadLocal.get() != null;
    }
    public static List<TimeRange> getTimeRangeList() {
        return threadLocal.get().timeRangeList;
    }
    public static int getLimit() {
        return threadLocal.get().limit;
    }
    public static boolean isQueryList() {
        return threadLocal.get().isQueryList;
    }
    public static TimeShardQuery getTimeShardQuery() {
        return threadLocal.get().timeShardQuery;
    }


    public static void clear() {
        threadLocal.remove();
    }

    public static void set(List<TimeRange> timeRangeList, TimeShardQuery timeShardQuery,int limit,  boolean isQueryList) {
        threadLocal.set(new TimeRangeQueryContext(timeRangeList, timeShardQuery, limit, isQueryList));
    }
    private static class TimeRangeQueryContext{
        private List<TimeRange> timeRangeList;
        private TimeShardQuery timeShardQuery;
        private int limit;
        private boolean isQueryList;

        public TimeRangeQueryContext(List<TimeRange> timeRangeList,  TimeShardQuery timeShardQuery, int limit, boolean isQueryList) {
            this.timeRangeList = timeRangeList;
            this.timeShardQuery = timeShardQuery;
            this.limit = limit;
            this.isQueryList = isQueryList;
        }
    }
}
