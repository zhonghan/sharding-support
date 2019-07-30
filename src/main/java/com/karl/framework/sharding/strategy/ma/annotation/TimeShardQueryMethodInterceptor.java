package com.karl.framework.sharding.strategy.ma.annotation;


import com.karl.framework.sharding.strategy.ma.DateType;
import com.karl.framework.sharding.strategy.ma.MonthArchiveShardStrategy;
import com.karl.framework.sharding.strategy.ma.TimeRange;
import com.karl.framework.sharding.strategy.ma.TimeWrapper;
import com.karl.framework.sharding.strategy.ma.holder.TimeShardingContextHolder;
import com.karl.framework.sharding.util.DateUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author karl.zhong
 * 如果查询的时间范围跨了归档表，则需要跨表查询，并将数据聚合。
 */
@Aspect
@Component
public class TimeShardQueryMethodInterceptor {
    private static final Logger logger  = LoggerFactory.getLogger(TimeShardQueryMethodInterceptor.class);
    @Autowired
    private MonthArchiveShardStrategy monthArchiveShardStrategy;
    @Pointcut("@annotation(com.karl.framework.sharding.strategy.ma.annotation.TimeShardQuery)")
    private void doIntercept() {

    }
    @Around(value = "doIntercept()")
    public Object around(ProceedingJoinPoint pjp) {
        try {
            Signature signature = pjp.getSignature();
            MethodSignature methodSignature = (MethodSignature)signature;
            Method method = methodSignature.getMethod();
            TimeShardQuery timeShardQuery = method.getAnnotation(TimeShardQuery.class);
            if(timeShardQuery == null) {
                return pjp.proceed();
            }
            String[] parameterNames = getParameterNames(method);

            TimeWrapper startTime = getStartTime(pjp, timeShardQuery, parameterNames);

            TimeWrapper endTime = getEndTime(pjp, timeShardQuery, parameterNames);

            List<TimeRange> timeRangeList = monthArchiveShardStrategy.getTimeRangeList(startTime, endTime);
            if(OrderStrategy.TIME_DESC.equals(timeShardQuery.orderStrategy())) {
                Collections.reverse(timeRangeList);
            }
            int limit = getLimit(pjp, parameterNames, timeShardQuery);
            Class retClazz = method.getReturnType();
            try {
                TimeShardingContextHolder.set(timeRangeList, timeShardQuery, limit, List.class.isAssignableFrom(retClazz));
                return pjp.proceed();
            }finally {
                TimeShardingContextHolder.clear();
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("unknown error:", throwable);
        }
    }

    private TimeWrapper getEndTime(ProceedingJoinPoint pjp, TimeShardQuery timeShardQuery, String[] parameterNames) {
        String endTimeExpressionStr = timeShardQuery.endTime();
        if(Constants.NO_END_TIME.equals(endTimeExpressionStr)) {
            //如果没有endTime，则默认设置为当前时间
            return new TimeWrapper(new Date());
        } else {
            TimeWrapper endTime = getValue(pjp, parameterNames, endTimeExpressionStr);
            if(endTime == null) {
                return new TimeWrapper(new Date());
            }
            if(endTime.getDate().compareTo(new Date()) > 0) {
                return new TimeWrapper(new Date(), endTime.getDateType());
            }
            return endTime;
        }
    }

    private TimeWrapper getStartTime(ProceedingJoinPoint pjp, TimeShardQuery timeShardQuery, String[] parameterNames) {
        String startTimeExpressionStr = timeShardQuery.startTime();
        if(Constants.NO_START_TIME.equals(startTimeExpressionStr)) {
            //如果没有startTime，则设置为最早归档表的开始时间。
            return new TimeWrapper(monthArchiveShardStrategy.getTheVeryStartTime());
        } else {
            TimeWrapper startTime = getValue(pjp, parameterNames, timeShardQuery.startTime());
            Date veryStartTime = monthArchiveShardStrategy.getTheVeryStartTime();
            if(startTime == null) {
                return new TimeWrapper(veryStartTime);
            }
            if(startTime.getDate().compareTo(veryStartTime) < 0) {
                //如果指定的startTime早于最早归档表的开始时间，则将startTime设置为最早归档表的开始时间。
                //避免找不到表的情况。
                return new TimeWrapper(veryStartTime, startTime.getDateType());
            }
            return startTime;
        }
    }

    private int getLimit(ProceedingJoinPoint pjp, String[] parameterNames,TimeShardQuery timeShardQuery) {
        if(timeShardQuery.limit().endsWith(Constants.NO_LIMITED)){
            return -1;
        }
        return getValue(pjp, parameterNames, timeShardQuery.limit(), Integer.class);
    }

    private <T> T getValue(ProceedingJoinPoint pjp, String[] parameterNames, String expressionStr, Class<T> clazz) {
        Object[] methodArguments = pjp.getArgs();
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(expressionStr);
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < methodArguments.length; i++) {
            context.setVariable(parameterNames[i], methodArguments[i]);
        }
        return expression.getValue(context, clazz);
    }

    private TimeWrapper getValue(ProceedingJoinPoint pjp, String[] parameterNames, String expressionStr) {
        Object obj = getValue(pjp, parameterNames, expressionStr, Object.class);
        if(obj == null) {
            return null;
        }
        if(obj instanceof Date) {
            return new TimeWrapper((Date) obj);
        }
        if(obj instanceof String) {
            String dateStr = (String)obj;
            if(dateStr.length() <= DateUtils.DATE_FORMAT.length()) {
                return new TimeWrapper(DateUtils.parseDate(dateStr), dateStr, DateType.DATE_TIME_STRING);
            }else {
                return new TimeWrapper(DateUtils.parseDate(dateStr, DateUtils.DATE_TIME_FORMAT), dateStr, DateType.DATE_STRING);
            }
        }
        return null;
    }

    public String[] getParameterNames(Method method){
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        return  u.getParameterNames(method);
    }
}
