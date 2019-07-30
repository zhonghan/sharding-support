package com.karl.framework.sharding.dao;


import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public abstract class MyBatisDao<E> {
    private SqlSessionFactoryBean sqlSessionFactoryBean;

    @Autowired
    public void setSqlSessionFactoryBean(SqlSessionFactoryBean sqlSessionFactoryBean) {
        this.sqlSessionFactoryBean = sqlSessionFactoryBean;
    }

    public List<E> selectList(final String aStatement, final Map<String, Object> aCondition, RowBounds aRowBounds) {
        SqlSession session = getSqlSession();
        return session.selectList(aStatement, aCondition, aRowBounds);
    }
    public SqlSession getSqlSession()  {
        try {
            return sqlSessionFactoryBean.getObject().openSession();
        } catch (Exception e) {
            return null;
        }
    }

}
