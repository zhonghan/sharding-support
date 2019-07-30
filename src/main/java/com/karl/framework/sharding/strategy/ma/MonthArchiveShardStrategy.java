package com.karl.framework.sharding.strategy.ma;


import com.karl.framework.sharding.exception.ShardingConfigurationException;
import com.karl.framework.sharding.strategy.ShardStrategy;
import com.karl.framework.sharding.util.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 一年做两次数据迁移，时间分别是5月1号0点和11月1号0点
 * @author karl.zhong
 */
public class MonthArchiveShardStrategy implements ShardStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MonthArchiveShardStrategy.class);
    /**
     * 保留的归档表数量, 比如一年有4个归档表，然后需要保留5年的数据，则keepArchiveTableNum 应该设置为5*4=20;
     * 如果不是根据时间查询时，则需要进行扫表，查查询的是单条数据时，则查到了就返回，如果查询是集合，则需要查询所有的归档表，每个归档表都需要查询一次。
     */
    private int keepArchiveTableNum;

    private List<TimeDuration> configTimeDurations;

    public MonthArchiveShardStrategy(int keepArchiveTableNum, List<TimeDuration> configTimeDurations) {
        this.keepArchiveTableNum = keepArchiveTableNum;
        this.configTimeDurations = configTimeDurations;
        if(configTimeDurations.get(0).getFromMonth() != configTimeDurations.get(configTimeDurations.size()-1).getToMonth() ||
        configTimeDurations.get(0).getFromDay() != configTimeDurations.get(configTimeDurations.size()-1).getToDay()) {
            throw new ShardingConfigurationException("configTimeDurations configuration error, should cover a whole year.");
        }
        if(configTimeDurations.size() > 1) {
            for (int i=1; i<configTimeDurations.size();i++) {
                TimeDuration preTd = configTimeDurations.get(i-1);
                TimeDuration curTd = configTimeDurations.get(i);
                if(curTd.getFromMonth() != preTd.getToMonth() || curTd.getFromDay() != preTd.getToDay()){
                    throw new ShardingConfigurationException("configTimeDurations configuration error, the end date of ["+(i-1)+"] timeDuration should equals with the date of ["+i+"] timeDuration..");
                }
            }
        }
    }

    public int getKeepArchiveTableNum() {
        return keepArchiveTableNum;
    }

    /**
     * 根据日期获取分表的后缀
     * @param date
     * @return
     */
    public String getShardingTableSuffix(TimeWrapper date) {
        Date previewTwoTimeArchiveDay = getPreviewTwoTimeArchiveDay();
        if(date.getDate().compareTo(previewTwoTimeArchiveDay) >= 0) {
            return "";
        }
        Calendar instance = Calendar.getInstance();
        instance.setTime(date.getDate());
        for(TimeDuration td : configTimeDurations) {
            int currentMonth = instance.get(Calendar.MONTH) +1;
            int currentDay = instance.get(Calendar.DATE);
            int currentYear = instance.get(Calendar.YEAR);
            int fromMonth = td.getFromMonth();
            int fromDay = td.getFromDay();
            int toMonth = td.getToMonth();
            int toDay = td.getToDay();
            if(fromMonth < toMonth) {
                if(inDuration(currentMonth, currentDay, fromMonth, fromDay, toMonth, toDay)) {
                    return generateSuffix(currentYear, td.getSuffix());
                }
            }else {
                if(inDuration(currentMonth, currentDay, fromMonth, fromDay, 12, 32) ||
                        inDuration(currentMonth, currentDay, 1,1, toMonth, toDay)) {
                    if(toMonth == 1 && toDay == 1) {
                        return generateSuffix(currentYear, td.getSuffix());
                    }
                    if(inDuration(currentMonth, currentDay, fromMonth, fromDay, 12, 32)) {
                        return generateSuffix(currentYear + 1, td.getSuffix());
                    } else {
                        return generateSuffix(currentYear, td.getSuffix());
                    }
                }
            }
        }
        throw new ShardingConfigurationException("no match archive duration for date:"+date);
    }

    private String generateSuffix(int year, String index) {
        if(StringUtils.isEmpty(index)) {
            return "_" +year;
        }else {
            return "_" + year + "_" + index;
        }
    }

    /**
     * 根据实践范围，返回分表的时间范围的List，因为每个表都有一个时间范围
     * @param startTime
     * @param endTime
     * @return
     */
    public List<TimeRange> getTimeRangeList(TimeWrapper startTime, TimeWrapper endTime) {
        List<TimeRange> list = new ArrayList<>();
        //前两次归档的日期到当前的数据都没有被归档掉。
        Date previewTwoTimeArchiveDay = getPreviewTwoTimeArchiveDay();
        if(startTime.getDate().compareTo(previewTwoTimeArchiveDay) >= 0) {
            list.add(new TimeRange(startTime, endTime, getShardingTableSuffix(startTime)));
            return list;
        }

        TimeWrapper nextArchiveDate = getNextArchiveDate(startTime);

        if(endTime.getDate().compareTo(nextArchiveDate.getDate()) > 0){
            list.add(new TimeRange(startTime, nextArchiveDate, getShardingTableSuffix(startTime)));
            list.addAll(getTimeRangeList(nextArchiveDate, endTime));
        }else {
            list.add(new TimeRange(startTime, endTime, getShardingTableSuffix(startTime)));
        }
        return list;
    }

    /**
     * 获取指定时间之后的归档日期
     * @param date
     * @return
     */
    private TimeWrapper getNextArchiveDate(TimeWrapper date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date.getDate());
        for(TimeDuration td : configTimeDurations) {
            int currentMonth = instance.get(Calendar.MONTH) +1;
            int currentDay = instance.get(Calendar.DATE);
            int currentYear = instance.get(Calendar.YEAR);
            int fromMonth = td.getFromMonth();
            int fromDay = td.getFromDay();
            int toMonth = td.getToMonth();
            int toDay = td.getToDay();
            if(fromMonth < toMonth) {
                if(inDuration(currentMonth, currentDay, fromMonth, fromDay, toMonth, toDay)) {
                    return new TimeWrapper(DateUtils.createDateWithoutTime(currentYear, toMonth, toDay), date.getDateType());
                }
            }else {
                if(inDuration(currentMonth, currentDay, fromMonth, fromDay, 12, 32) ||
                        inDuration(currentMonth, currentDay, 1,1, toMonth, toDay)) {
                    if(inDuration(currentMonth, currentDay, fromMonth, fromDay, 12,32)){
                        return new TimeWrapper(DateUtils.createDateWithoutTime(currentYear+1, toMonth, toDay), date.getDateType());
                    }
                    return new TimeWrapper(DateUtils.createDateWithoutTime(currentYear, toMonth, toDay), date.getDateType());
                }
            }
        }
        throw new ShardingConfigurationException("getLatestArchiveDay error, date:"+date+", please check configuration.");
    }

    public Date getPreviewTwoTimeArchiveDay() {
        Date latestArchiveDay = getLatestArchiveDay(new Date());
        Calendar instance = Calendar.getInstance();
        instance.setTime(latestArchiveDay);
        //边界值减一天，则不是边界值了。
        instance.add(Calendar.DATE, -1);
        return getLatestArchiveDay(instance.getTime());
    }

    /**
     * 获取最早的时间
     * @return
     */
    public Date getTheVeryStartTime() {
        Date date = this.getLatestArchiveDay(new Date());
        for(int i=0;i<this.getKeepArchiveTableNum() + 1;i++) {
            date = DateUtils.addDays(date, -1);
            date = this.getLatestArchiveDay(date);
        }
        return date;
    }

    /**
     * 获取指定日期前面最近的归档日期
     * @return
     */
    public Date getLatestArchiveDay(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        for(TimeDuration td : configTimeDurations) {
            int currentMonth = instance.get(Calendar.MONTH) +1;
            int currentDay = instance.get(Calendar.DATE);
            int currentYear = instance.get(Calendar.YEAR);
            int fromMonth = td.getFromMonth();
            int fromDay = td.getFromDay();
            int toMonth = td.getToMonth();
            int toDay = td.getToDay();
            if(fromMonth < toMonth) {
                if (inDuration(currentMonth, currentDay, fromMonth, fromDay, toMonth, toDay)) {
                    return DateUtils.createDateWithoutTime(currentYear, fromMonth, fromDay);
                }
            } else if(fromMonth == toMonth) {
                if(currentMonth < fromMonth) {
                    return DateUtils.createDateWithoutTime(currentYear - 1, fromMonth, fromDay);
                } else {
                    return DateUtils.createDateWithoutTime(currentYear, fromMonth, fromDay);
                }
            }else {
                if(toMonth == 1 && toDay == 1) {
                    return DateUtils.createDateWithoutTime(currentYear, fromMonth, fromDay);
                }else {
                    if (inDuration(currentMonth, currentDay, fromMonth, fromDay, 12, 32) ||
                            inDuration(currentMonth, currentDay, 1, 1, toMonth, toDay)) {
                        return DateUtils.createDateWithoutTime(currentYear - 1, fromMonth, fromDay);
                    }
                }
            }
        }
        throw new ShardingConfigurationException("getLatestArchiveDay error, date:"+date+", please check configuration.");
    }



    private boolean inDuration(int month, int day, int fromMonth, int fromDay, int toMonth, int toDay) {
        return bigOrEqual(month, day, fromMonth, fromDay) && lessThan(month, day, toMonth, toDay);
    }

    private boolean lessThan(int month, int day, int toMonth, int toDay) {
        if(month < toMonth) {
            return true;
        }
        return month == toMonth && day < toDay;
    }

    private boolean bigOrEqual(int month, int day, int fromMonth, int fromDay) {
        if(month > fromMonth) {
            return true;
        }
        return month == fromMonth && day  >= fromDay;
    }

}
