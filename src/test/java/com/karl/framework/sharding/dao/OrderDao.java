package com.karl.framework.sharding.dao;


import com.karl.framework.sharding.model.Order;
import com.karl.framework.sharding.service.order.OrderTimeQuery;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderDao {

    List<Order> findByCreTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("startIndex") int startIndex, @Param("pageSize") int pageSize);

    Integer getOrderCount(@Param("beginDate") Date startTime, @Param("endDate") Date endTime);

    List<Order> getByDesc(@Param("desc") String desc);

    Order getById(@Param("id")Long id);

    List<Order> findByMap(@Param("map")Map<String, Object> map, @Param("startIndex")int startIndex, @Param("pageSize")int pageSize);

    Integer getOrderCountByMap(Map<String, Object> map);

    Integer getOrderCountByOrderTimeQuery(@Param("query")OrderTimeQuery query);

    List<Order> findByOrderTimeQuery(@Param("query")OrderTimeQuery query, @Param("startIndex")int startIndex, @Param("pageSize")int pageSize);

    List<Order> getOrders(@Param("beginDate") String beginDate, @Param("endDate")String endDate, @Param("startIndex") int startIndex, @Param("pageSize")int pageSize);
}