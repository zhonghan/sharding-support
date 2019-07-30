package com.karl.framework.sharding.dao;

import com.karl.framework.sharding.model.Order;
import com.karl.framework.sharding.strategy.ma.annotation.QueryType;
import com.karl.framework.sharding.strategy.ma.annotation.TimeShardQuery;

import com.github.pagehelper.PageInfo;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository extends MyBatisDao<Order> {

    @TimeShardQuery(startTime = "#condition[beginDate]", endTime = "#condition[endDate]", limit = "#pageSize", queryType = QueryType.PAGE_HELPER)
    public PageInfo<Order> query(Map<String, Object> condition, int pageNo, int pageSize) {
        List<Order> list = selectList("com.karl.framework.sharding.dao.OrderDao.query", condition, new RowBounds(
                pageNo, pageSize));
        return new PageInfo<Order>(list);
    }
}
