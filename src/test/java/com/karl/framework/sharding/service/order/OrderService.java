package com.karl.framework.sharding.service.order;


import com.karl.framework.sharding.dao.OrderDao;
import com.karl.framework.sharding.dao.OrderDaoWrapper;
import com.karl.framework.sharding.dao.OrderRepository;
import com.karl.framework.sharding.model.Order;
import com.karl.framework.sharding.strategy.ma.annotation.TimeShardQuery;
import com.karl.framework.sharding.util.Pagination;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private OrderDaoWrapper orderDaoWrapper;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDao orderDao;
    @TimeShardQuery(startTime = "#startTime", endTime = "#endTime",offsetParamName = "startIndex", limit = "#pageSize")
    public List<Order> getOrders(String startTime, String endTime, int pageNo, int pageSize){
        return orderDao.getOrders(startTime, endTime, (pageNo-1) * pageSize, pageSize);
    }

    public Pagination getOrders(Date startTime, Date endTime, int pageNo, int pageSize){
        Integer total = orderDaoWrapper.getOrderCount(startTime, endTime);
        if(total>0) {
            List<Order> list = orderDaoWrapper.findByCreTime(startTime, endTime, (pageNo-1) * pageSize, pageSize);
            return new Pagination(total, pageSize, pageNo, list);
        }
        return new Pagination(total, pageSize,pageNo, null);
    }

    public Pagination getOrders2(Date startTime, Date endTime, int pageNo, int pageSize){
        Map<String, Object> map = new HashMap<>();
        map.put("beginDate", startTime);
        map.put("endDate", endTime);
        long beginTime = System.currentTimeMillis();
        Integer total = orderDaoWrapper.getOrderCountByMap(map);
        logger.info("count usetime:{} ms", (System.currentTimeMillis()-beginTime));
        beginTime = System.currentTimeMillis();
        if(total>0) {
            List<Order> list = orderDaoWrapper.findByCreTime(map, (pageNo-1) * pageSize, pageSize);
            Pagination pagination = new Pagination(total, pageSize, pageNo, list);
            logger.info("list usetime:{} ms", (System.currentTimeMillis()-beginTime));
            return pagination;
        }

        return new Pagination(total, pageSize,pageNo, null);
    }

    public Pagination getOrdersWithoutStartTime(Date endTime, int pageNo, int pageSize){
        Integer total = orderDaoWrapper.getOrderCount(endTime);
        if(total>0) {
            List<Order> list = orderDaoWrapper.findByCreTime(endTime, (pageNo-1) * pageSize, pageSize);
            return new Pagination(total, pageNo, pageSize, list);
        }
        return new Pagination(total, pageSize,pageNo, null);
    }

    public Pagination getOrdersByStartTime(Date startTime, int pageNo, int pageSize){
        Integer total = orderDaoWrapper.getOrderCountByStartTime(startTime);
        if(total>0) {
            List<Order> list = orderDaoWrapper.findByCreTimeByStartTime(startTime, (pageNo-1) * pageSize, pageSize);
            return new Pagination(total, pageNo, pageSize, list);
        }
        return new Pagination(total, pageSize, pageNo, null);
    }

    @TimeShardQuery
    public List<Order> getByDesc(String desc) {
        return orderDaoWrapper.getByDesc(desc);
    }

    @TimeShardQuery
    public Order getById(Long id) {
        return orderDao.getById(id);
    }

    public Pagination getOrdersWithObjectParam(Date startTime, Date endTime, int pageNo, int pageSize) {
        OrderTimeQuery query = new OrderTimeQuery(startTime, endTime );
        Integer total = orderDaoWrapper.getOrderCount(query);
        if(total>0) {
            List<Order> list = orderDaoWrapper.findByCreTime(query, (pageNo-1) * pageSize, pageSize);
            return new Pagination(total, pageSize, pageNo, list);
        }
        return new Pagination(total, pageSize,pageNo, null);
    }

    public PageInfo<Order> getOrderByPageHelper(Map<String, Object> map , int pageNo, int pageSize){
        return orderRepository.query(map, pageNo, pageSize);
    }
}