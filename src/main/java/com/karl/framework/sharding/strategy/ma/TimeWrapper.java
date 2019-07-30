package com.karl.framework.sharding.strategy.ma;

import java.util.Date;

/**
 * @author karl.zhong
 */
public class TimeWrapper {
    private Date date;
    private String dateStr;
    private DateType dateType;

    public TimeWrapper(Date date) {
        this.date = date;
        this.dateType = DateType.JAVA_UTIL_DATE;
    }

    public TimeWrapper(Date date, DateType dateType) {
        this.date = date;
        this.dateType = dateType;
    }
    public TimeWrapper(Date date, String dateStr, DateType dateType) {
        this.date = date;
        this.dateStr = dateStr;
        this.dateType = dateType;
    }

    public Date getDate() {
        return date;
    }

    public TimeWrapper setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getDateStr() {
        return dateStr;
    }

    public TimeWrapper setDateStr(String dateStr) {
        this.dateStr = dateStr;
        return this;
    }

    public DateType getDateType() {
        return dateType;
    }

    public TimeWrapper setDateType(DateType dateType) {
        this.dateType = dateType;
        return this;
    }

    @Override
    public String toString() {
        return "TimeWrapper{" +
                "date=" + date +
                ", dateStr='" + dateStr + '\'' +
                ", dateType=" + dateType +
                '}';
    }
}
