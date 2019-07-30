package com.karl.framework.sharding.strategy.ma;

import com.karl.framework.sharding.exception.ShardingConfigurationException;

/**
 * @author karl.zhong
 */
public class TimeDuration {
    private int fromMonth;
    private int fromDay;
    private int toMonth;
    private int toDay;
    private String suffix;

    public TimeDuration(String fromDate, String toDate) {
        this(fromDate, toDate, "");
    }
    public TimeDuration(String fromDate, String toDate, String suffix) {
        if(fromDate.indexOf(0) == '0'){
            fromDate = fromDate.substring(1);
        }
        if(toDate.indexOf(0) == '0'){
            toDate = toDate.substring(1);
        }
        String[] from = fromDate.split("-");
        if(from.length != 2){
            throw new ShardingConfigurationException("configTimeDurations configuration error, key:["+from+"] format invalid. the format should be MM-DD");
        }
        this.fromMonth = Integer.valueOf(from[0]);
        this.fromDay = Integer.valueOf(from[1]);
        String[] to = toDate.split("-");
        if(to.length != 2){
            throw new ShardingConfigurationException("configTimeDurations configuration error, key:["+to+"] format invalid. the format should be MM-DD");
        }
        this.toMonth = Integer.valueOf(to[0]);
        this.toDay = Integer.valueOf(to[1]);
        this.suffix = suffix;
    }


    public int getFromMonth() {
        return fromMonth;
    }

    public int getFromDay() {
        return fromDay;
    }

    public int getToMonth() {
        return toMonth;
    }

    public int getToDay() {
        return toDay;
    }

    public String getSuffix() {
        return suffix;
    }
}
