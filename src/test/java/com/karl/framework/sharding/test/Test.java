package com.karl.framework.sharding.test;

import com.karl.framework.sharding.strategy.ma.MonthArchiveShardStrategy;
import com.karl.framework.sharding.strategy.ma.TimeRange;
import com.karl.framework.sharding.strategy.ma.TimeWrapper;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Test {
    private Logger logger = LoggerFactory.getLogger(Test.class);
    private static ClassPathXmlApplicationContext context;
    private static MonthArchiveShardStrategy strategy;

    @BeforeClass
    public static void beforeClass() {
        context = new ClassPathXmlApplicationContext("classpath:spring/spring-context.xml");
        context.start();
        strategy = (MonthArchiveShardStrategy)context.getBean("monthArchiveShardStrategy");
    }

    @org.junit.Test
    public void test() {
        List<TimeRange> list = strategy.getTimeRangeList(new TimeWrapper(getDate("2017-11-02")), new TimeWrapper(getDate("2019-6-23")));
        for(TimeRange tr : list) {
            String suffix = strategy.getShardingTableSuffix(tr.getStartTime());
            logger.info("suffix"+suffix + " of startTime[" + format(tr.getStartTime().getDate())+","+ format(tr.getEndTime().getDate())+")");
        }

    }

    private Date getDate(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(s);
        } catch (ParseException e) {
            logger.info(e.getMessage(), e);
            return null;
        }
    }

    private String format(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
