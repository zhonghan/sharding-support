package com.karl.framework.sharding.strategy.ma.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 不能加载抽象方法上。
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TimeShardQuery {
    /**
     * format yyyy-MM-dd
     * 开始时间， 使用EL表达式从参数中取数
     * 没有开始时间就使用不需要赋值，系统会自动计算最早的一个归档表开始， 跟参数keepArchiveTableNum相关。
     * @return
     */
    String startTime() default Constants.NO_START_TIME;
    /**
     * format yyyy-MM-dd
     * 结束时间，使用EL表达式从参数中取数
     * 没有结束时间，则当前时间为默认的结束时间。
     * @return
     */
    String endTime() default Constants.NO_END_TIME;

    /**
     * 查询类型
     * @see QueryType
     * @return
     */
    QueryType queryType() default  QueryType.DEFAULT;

    /**
     * 查询数量，分页是需要用， 使用EL表达式从参数中取数
     * @return
     */
    String limit() default Constants.NO_LIMITED;

    String offsetParamName() default "First_PageHelper";

    /**
     * 按时间的排序规则。默认是顺序，目前有如下两种选择
     * OrderStrategy.ASC
     * OrderStrategy.DESC
     * OrderStrategy.OTHER 赞不支持
     * @return
     */
    OrderStrategy orderStrategy() default OrderStrategy.TIME_DESC;
}
