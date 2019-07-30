package com.karl.framework.sharding.dao;


import com.karl.framework.sharding.model.Order;
import com.karl.framework.sharding.service.order.OrderTimeQuery;
import com.karl.framework.sharding.strategy.ma.annotation.OrderStrategy;
import com.karl.framework.sharding.strategy.ma.annotation.QueryType;
import com.karl.framework.sharding.strategy.ma.annotation.TimeShardQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderDaoWrapper {

    @Autowired
    private OrderDao orderDao;

    public List<Order> getByDesc(String desc) {
        List<Order> list = orderDao.getByDesc(desc);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list;
    }

    @TimeShardQuery(startTime = "#map[beginDate]", endTime = "#map[endDate]", offsetParamName = "startIndex", limit = "#pageSize", orderStrategy = OrderStrategy.TIME_DESC)
    public List<Order> findByCreTime(Map<String, Object> map, int startIndex, int pageSize) {
        return orderDao.findByMap(map, startIndex, pageSize);
    }
    @TimeShardQuery(startTime = "#map[beginDate]", endTime = "#map[endDate]", queryType = QueryType.COUNT)
    public Integer getOrderCountByMap(Map<String, Object> map) {
        return orderDao.getOrderCountByMap(map);
    }

    @TimeShardQuery(startTime = "#startTime", endTime = "#endTime",offsetParamName = "startIndex", limit = "#pageSize", orderStrategy = OrderStrategy.TIME_DESC)
    public List<Order> findByCreTime(Date startTime, Date endTime, int startIndex, int pageSize) {
        return orderDao.findByCreTime(startTime, endTime, startIndex, pageSize);
    }

    @TimeShardQuery(startTime = "#beginDate", endTime = "#endDate", queryType = QueryType.COUNT)
    public Integer getOrderCount(Date beginDate, Date endDate) {
        return orderDao.getOrderCount(beginDate, endDate);
    }

    @TimeShardQuery(endTime = "#endTime", limit = "#pageSize", offsetParamName = "startIndex")
    public List<Order> findByCreTime(Date endTime, int startIndex, int pageSize) {
        return orderDao.findByCreTime(null, endTime, startIndex, pageSize);
    }
    @TimeShardQuery(endTime = "#endDate", queryType = QueryType.COUNT)
    public Integer getOrderCount(Date endDate) {
        return orderDao.getOrderCount(null ,endDate);
    }
    @TimeShardQuery(startTime = "#startTime", queryType = QueryType.COUNT)
    public Integer getOrderCountByStartTime(Date startTime) {
        return orderDao.getOrderCount(startTime ,null);
    }

    @TimeShardQuery(startTime = "#startTime", limit = "#pageSize", offsetParamName = "startIndex", orderStrategy = OrderStrategy.TIME_ASC)
    public List<Order> findByCreTimeByStartTime(Date startTime, int startIndex, int pageSize) {
        return orderDao.findByCreTime(startTime, null, startIndex, pageSize);
    }
    @TimeShardQuery(startTime = "#query.beginDate",endTime = "#query.endDate", queryType = QueryType.COUNT)
    public Integer getOrderCount(OrderTimeQuery query) {
        return orderDao.getOrderCountByOrderTimeQuery(query);
    }
    @TimeShardQuery(startTime = "#query.beginDate",endTime = "#query.endDate", offsetParamName = "startIndex", limit = "#pageSize")
    public List<Order> findByCreTime(OrderTimeQuery query, int startIndex, int pageSize) {
        return orderDao.findByOrderTimeQuery(query, startIndex, pageSize);
    }
}
