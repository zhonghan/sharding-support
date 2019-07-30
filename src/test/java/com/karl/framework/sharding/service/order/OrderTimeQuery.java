package com.karl.framework.sharding.service.order;

import java.util.Date;

public class OrderTimeQuery {
    private Date beginDate;
    private Date endDate;

    public OrderTimeQuery(Date beginDate, Date endDate) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public OrderTimeQuery setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
        return this;
    }

    public Date getEndDate() {
        return endDate;
    }

    public OrderTimeQuery setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }
}
