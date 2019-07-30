package com.karl.framework.sharding.test;

import com.karl.framework.sharding.model.Order;
import com.karl.framework.sharding.service.order.OrderService;
import com.karl.framework.sharding.util.Pagination;

import com.github.pagehelper.PageInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardingTest {
    private Logger logger = LoggerFactory.getLogger(ShardingTest.class);
    private static ClassPathXmlApplicationContext context;
    private static OrderService orderService;
    @BeforeClass
    public static void beforeClass() {
        context = new ClassPathXmlApplicationContext("classpath:spring/spring-context.xml");
        context.start();
        orderService = context.getBean(OrderService.class);
    }

    @Test
    public void testPageHelper() {
        Map<String,Object> map = new HashMap<>();
        map.put("beginDate", getDate("2012-10-31"));
        map.put("endDate", getDate("2019-7-2"));
        PageInfo<Order> pageInfo = orderService.getOrderByPageHelper(map, 1,3);
        List<Order> list = pageInfo.getList();
        StringBuilder sb = new StringBuilder();
        for(Order o : list) {
            sb.append(o.toString() +" ");
        }
        logger.info(sb.toString());
        Assert.assertEquals(3, pageInfo.getList().size());
    }
    @Test
    public void testFindOrder() {
        Long beginTime = System.currentTimeMillis();
        Pagination orders = orderService.getOrders2(getDate("2018-10-31"), getDate("2019-5-1"), 1, 10);
        logger.info((System.currentTimeMillis()-beginTime)+"ms: " + orders);
        Assert.assertEquals(3, orders.getTotalSize());
    }
    @Test
    public void testFindOrder2() {
        Pagination orders = orderService.getOrders(getDate("2012-10-31"), getDate("2019-5-1"), 1, 10);
        logger.info("" + orders);
    }
    @Test
    public void testFindOrder3() {
//        //跨归档表
        Pagination orders = orderService.getOrdersWithoutStartTime(getDate("2019-5-1"), 1, 1);
        logger.info("" + orders);
        Assert.assertEquals(8, orders.getTotalSize());
    }
    @Test
    public void testFindOrder4() {
        Pagination orders3 = orderService.getOrdersByStartTime(getDate("2012-5-1"), 1, 10);
        Assert.assertEquals(11, orders3.getTotalSize());
        logger.info("" + orders3);
    }

    @Test
    public void testFindOrder5() {
        Pagination orders4 = orderService.getOrdersWithObjectParam(getDate("2012-10-31"), getDate("2019-5-1"), 1, 10);
        logger.info("" + orders4);
    }
    @Test
    public void testFindOrder6() {
        List<Order> orders5 = orderService.getOrders("2012-10-31", "2019-5-1", 1, 10);
        logger.info("" + orders5);
    }

    @Test
    public void testFindOrder8() {
        Map<String,Object> map = new HashMap<>();
        map.put("beginDate", getDate("2018-7-1"));
        map.put("endDate", getDate("2018-8-30"));
        PageInfo<Order> pageInfo = orderService.getOrderByPageHelper(map, 1,3);
        Assert.assertEquals(7L, pageInfo.getList().get(0).getId().longValue());
    }
    @Test
    public void testFindOrder9() {
        Map<String,Object> map = new HashMap<>();
        map.put("beginDate", "2017-4-2");
        map.put("endDate", "2017-4-3");
        PageInfo<Order> pageInfo = orderService.getOrderByPageHelper(map, 1,3);
        Assert.assertEquals(2L, pageInfo.getList().get(0).getId().longValue());
        Assert.assertEquals(1, pageInfo.getTotal());
    }
    @Test
    public void testFindOrder10() {
        Map<String,Object> map = new HashMap<>();
        map.put("beginDate", "2017-7-1");
        map.put("endDate", "2018-8-30");
        PageInfo<Order> pageInfo = orderService.getOrderByPageHelper(map, 1,3);
        Assert.assertEquals(7L, pageInfo.getList().get(0).getId().longValue());
        Assert.assertEquals(4, pageInfo.getTotal());
    }
    @Test
    public void testFindOrder7() {
        Pagination orders = orderService.getOrders(getDate("2018-7-1"),getDate("2018-8-30"), 1, 10);
        Assert.assertEquals(1, orders.getTotalSize());
    }

    @Test
    public void testNoShardingKey() {
        List<Order> orderList = orderService.getByDesc("a");
        Assert.assertEquals(1, orderList.size());
    }

    @Test
    public void testNoShardingKey2() {
        Order order = orderService.getById(20L);
        Assert.assertEquals("22222", order.getAddress());
        Assert.assertEquals("20", order.getDesc());
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
}
