package com.karl.framework.sharding.plugin;


import com.karl.framework.sharding.exception.ExecuteShardingSqlException;
import com.karl.framework.sharding.exception.ShardingConfigurationException;
import com.karl.framework.sharding.plugin.config.ShardPluginConfiguration;
import com.karl.framework.sharding.plugin.config.TableShardStrategyConfig;
import com.karl.framework.sharding.plugin.util.ShardingExecuteThreadFactory;
import com.karl.framework.sharding.strategy.ma.DateType;
import com.karl.framework.sharding.strategy.ma.TimeRange;
import com.karl.framework.sharding.strategy.ma.TimeWrapper;
import com.karl.framework.sharding.strategy.ma.annotation.QueryType;
import com.karl.framework.sharding.strategy.ma.annotation.TimeShardQuery;
import com.karl.framework.sharding.strategy.ma.holder.TimeShardingContextHolder;
import com.karl.framework.sharding.util.ApplicationContextUtils;
import com.karl.framework.sharding.util.ReflectionUtils;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author karl.zhong
 * @support mybatis3.4
 */
@Service
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class TimeShardQueryPlugin implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(TimeShardQueryPlugin.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if(TimeShardingContextHolder.exist()){
            return process(invocation);
        }
        return invocation.proceed();
    }

    private Object process(Invocation invocation)  {
        //获取拦截方法的参数
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        ResultHandler resultHandler = (ResultHandler) args[3];
        //当前的目标对象
        Executor executor = (Executor) invocation.getTarget();

        if(TimeShardingContextHolder.getTimeShardQuery().queryType().equals(QueryType.PAGE_HELPER)) {
            BoundSql boundSql = (BoundSql)args[5];
            CacheKey cacheKey = (CacheKey)args[4];
            if(isCount(cacheKey.toString())){
                return handleQuery(TimeShardingContextHolder.getTimeRangeList(),
                        executor, resultHandler, boundSql, ms,parameterObject, true);
            }else {
                return handlePageListQuery(TimeShardingContextHolder.getTimeRangeList(),
                        executor, resultHandler, boundSql, ms,parameterObject);
            }
        }else {
            BoundSql boundSql = ms.getBoundSql(parameterObject);
            if(TimeShardingContextHolder.getTimeShardQuery().queryType().equals(QueryType.COUNT)){
                return handleQuery(TimeShardingContextHolder.getTimeRangeList(),
                        executor, resultHandler, boundSql, ms,parameterObject, true);
            }
            if(TimeShardingContextHolder.isQueryList()){
                if(TimeShardingContextHolder.getLimit() != -1){
                    return handlePageListQuery(TimeShardingContextHolder.getTimeRangeList(),
                            executor, resultHandler, boundSql, ms,parameterObject);
                }
                return handleQuery(TimeShardingContextHolder.getTimeRangeList(),
                        executor, resultHandler, boundSql, ms,parameterObject, false);
            }else {
                return handleSingleObjQuery(TimeShardingContextHolder.getTimeRangeList(),
                        executor, resultHandler, boundSql, ms,parameterObject);
            }
        }
    }

    /**
     * 查询单个对象，查到就返回。
     */
    private Object handleSingleObjQuery(List<TimeRange> timeRangeList, Executor executor, ResultHandler resultHandler,
                                        BoundSql boundSql, MappedStatement ms, Object parameterObject) {
        for (TimeRange timeRange : timeRangeList) {
            Object retObj = executeSql(executor, resultHandler, boundSql, timeRange, ms, parameterObject);
            if (retObj != null) {
                return retObj;
            }
        }
        return null;
    }

    /**
     * 分页查询时，只能一个表一个表的查询。
     */
    private Object handlePageListQuery(List<TimeRange> timeRangeList,Executor executor, ResultHandler resultHandler,
                                       BoundSql boundSql, MappedStatement ms, Object parameterObject)  {
        String offsetParamName = TimeShardingContextHolder.getTimeShardQuery().offsetParamName();
        Integer offset = (Integer)((Map)parameterObject).get(offsetParamName);
        ((Map)parameterObject).put(offsetParamName, 0);
        int limit = TimeShardingContextHolder.getLimit() + offset;
        List retList = null;
        for(TimeRange timeRange: timeRangeList) {
            //将查询中的参数，开始时间与结束时间替换掉。

            List tempList = (List)executeSql(executor ,resultHandler, boundSql, timeRange, ms, parameterObject);

            if(retList == null) {
                retList = tempList;
                if(retList != null && retList.size() == limit) {
                    break;
                }
            }else {
                if(tempList != null) {
                    if(retList.size() + tempList.size() < limit) {
                        retList.addAll(tempList);
                    }else {
                        int leftSpace = limit - retList.size();
                        retList.addAll(tempList.subList(0, leftSpace));
                        break;
                    }
                }
            }
        }
        if(retList != null) {
            if(retList.size() > offset) {
                return retList.subList(offset,retList.size());
            }else {
                return retList.subList(0,0);
            }
        }else {
            return retList;
        }
    }

    private Object handleQuery(List<TimeRange> timeRangeList, Executor executor,
                                          ResultHandler resultHandler,BoundSql boundSql,
                                          MappedStatement ms, Object parameterObject, boolean isCount) {
        if(CollectionUtils.isEmpty(timeRangeList)) {
            throw new ShardingConfigurationException("timeRangeList can not be empty");
        }
        if(timeRangeList.size() == 1) {
            return executeSql(executor ,resultHandler, boundSql, timeRangeList.get(0), ms, parameterObject);
        }
        return multiThreadHandleQuery(TimeShardingContextHolder.getTimeRangeList(),
                executor, resultHandler, boundSql, ms,parameterObject, isCount);


    }

    private Object multiThreadHandleQuery(List<TimeRange> timeRangeList, Executor executor,
                                               ResultHandler resultHandler,BoundSql boundSql,
                                               MappedStatement ms, Object parameterObject, boolean isCount) {
        int threadPoolSize = getTheadPoolSize(timeRangeList.size());
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize, new ShardingExecuteThreadFactory());
        CompletionService<Object> completionService = new ExecutorCompletionService<>(executorService);
        for (final TimeRange timeRange : timeRangeList) {
            completionService.submit(new QueryExecute(executor, resultHandler, boundSql, ms,timeRange,parameterObject, TimeShardingContextHolder.getTimeShardQuery()));
        }

        try {
            if(isCount) {
                Integer count = 0;
                Long lcount = 0L;
                boolean isInteger = false;
                for (int i = 0; i < timeRangeList.size(); i++) {
                    Object countResultList = completionService.take().get(5, TimeUnit.MINUTES);
                    if (countResultList == null) {
                        continue;
                    }
                    Object obj = ((List) countResultList).get(0);
                    if (obj instanceof Long) {
                        lcount += (Long) obj;
                    }
                    if (obj instanceof Integer) {
                        isInteger = true;
                        count += (Integer) obj;
                    }
                }
                if(isInteger) {
                    return Arrays.asList(count);
                }else {
                    return Arrays.asList(lcount);
                }
            }else {
                List<Object> totalList = null;
                for (int i = 0; i < timeRangeList.size(); i++) {
                    Object resultList = completionService.take().get(5, TimeUnit.MINUTES);
                    if (resultList == null) {
                        continue;
                    }
                    List list = (List) resultList;
                    if(totalList == null) {
                        totalList = list;
                    }else {
                        totalList.addAll(list);
                    }
                }
                return totalList;
            }
        }catch (Exception e) {
            logger.error("multiThreadHandleCountQuery error: boundSql:{}",boundSql.getSql(), e);
            return Arrays.asList(0L);
        }finally{
            executorService.shutdown();
        }
    }

    private int getTheadPoolSize(int taskNum) {
        int coreSize = Runtime.getRuntime().availableProcessors();
        if(coreSize > taskNum) {
            return taskNum;
        }
        return coreSize;
    }

    class QueryExecute implements Callable<Object> {
        private Executor executor;
        private ResultHandler resultHandler;
        private BoundSql boundSql;
        private MappedStatement ms;
        private TimeRange timeRange;
        private Object parameterObject;
        private TimeShardQuery timeShardQuery;

        public QueryExecute(Executor executor, ResultHandler resultHandler, BoundSql boundSql, MappedStatement ms,
                            TimeRange timeRange, Object parameterObject, TimeShardQuery timeShardQuery) {
            this.executor = executor;
            this.resultHandler = resultHandler;
            this.boundSql = boundSql;
            this.ms = ms;
            this.timeRange = timeRange;
            this.parameterObject = parameterObject;
            this.timeShardQuery = timeShardQuery;
        }

        @Override
        public Object call() {
            try{
                TimeShardingContextHolder.setTimeShardQuery(timeShardQuery);
                return executeSql(this.executor ,this.resultHandler, this.boundSql, this.timeRange, this.ms, this.parameterObject);
            }finally {
                TimeShardingContextHolder.clear();
            }

        }
    }

    private Object executeSql(Executor executor,ResultHandler resultHandler,BoundSql boundSql, TimeRange timeRange, MappedStatement ms, Object paramObject) {
        try {
            String sql = getSql(boundSql.getSql(), timeRange);
            logger.info("MySharding:suffix:[{}], sql:{}", timeRange.getTableSuffix(), sql);
            if(logger.isDebugEnabled()) {
                logger.debug("sharding-support begin execute sql:{}", sql);
            }
            final Object parameterObject = cloneParameterObject(paramObject);
            rewriteParameterObject(parameterObject, TimeShardingContextHolder.getTimeShardQuery(), timeRange);
            Field additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
            Map<String, Object> additionalParameters = (Map<String, Object>) additionalParametersField.get(boundSql);
            BoundSql subBoundSql = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(), parameterObject);
            for (String key : additionalParameters.keySet()) {
                subBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
            }
            CacheKey key = executor.createCacheKey(ms, parameterObject, RowBounds.DEFAULT, boundSql);
            key.update(timeRange.getTableSuffix());
            Object ret = executor.query(ms,parameterObject,RowBounds.DEFAULT,resultHandler,key,subBoundSql);
            if(logger.isDebugEnabled()) {
                logger.debug("sharding-support end execute sql:{}", sql);
            }
            return ret;
        }catch (Exception e) {
            logger.error("handleCountQuery timeRange:{}", timeRange, e);
            throw new ExecuteShardingSqlException("handleCountQuery timeRange:"+timeRange, e);
        }
    }

    private Object cloneParameterObject(Object paramObject) {
        if(paramObject instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap<Object> target = new MapperMethod.ParamMap<>();
            MapperMethod.ParamMap<Object> source = (MapperMethod.ParamMap<Object>) paramObject;
            for(MapperMethod.ParamMap.Entry<String, Object> entry :  source.entrySet()) {
                target.put(entry.getKey(), entry.getValue());
            }
            return target;
        }
        if(paramObject instanceof HashMap) {
            HashMap<String, Object> target = new HashMap<>();
            HashMap<String, Object> source = (HashMap<String, Object>) paramObject;
            for(HashMap.Entry<String, Object> entry :  source.entrySet()) {
                target.put(entry.getKey(), entry.getValue());
            }
            return target;
        }
        try {
            Object newObj = paramObject.getClass().newInstance();
            BeanUtils.copyProperties(paramObject, newObj);
            return newObj;
        }catch (Exception e) {
            logger.error("BeanUtils.copyProperties error", e);
        }
        return paramObject;
    }

    private void rewriteParameterObject(Object parameterObject, TimeShardQuery timeShardQuery, TimeRange timeRange) {
        if(parameterObject instanceof Map) {
            rewriteParameterObject((Map<String, Object>)parameterObject, TimeShardingContextHolder.getTimeShardQuery(), timeRange);
            return;
        }
        String startTimeKey = timeShardQuery.startTime().substring(1);
        String endTimeKey = timeShardQuery.endTime().substring(1);
        startTimeKey = startTimeKey.substring(startTimeKey.indexOf('.') + 1);
        endTimeKey = endTimeKey.substring(endTimeKey.indexOf('.') + 1);
        ReflectionUtils.setFieldValue(parameterObject, startTimeKey, getTime(timeRange.getStartTime()));
        ReflectionUtils.setFieldValue(parameterObject, endTimeKey, getTime(timeRange.getEndTime()));
    }

    private void rewriteParameterObject(Map<String, Object> parameterObject, TimeShardQuery timeShardQuery, TimeRange timeRange) {
        String startTimeEL = timeShardQuery.startTime().substring(1);
        String endTimeEL = timeShardQuery.endTime().substring(1);
        String startTimeKey = startTimeEL;
        String endTimeKey = endTimeEL;

        if(startTimeEL.indexOf('.') > -1 ){
            String key = startTimeEL.substring(0, startTimeEL.indexOf('.'));
            startTimeKey = startTimeEL.substring(startTimeEL.indexOf('.') + 1);
            endTimeKey = endTimeEL.substring(endTimeEL.indexOf('.') + 1);
            if(parameterObject.containsKey(key)){
                Object obj = parameterObject.get(key);
                ReflectionUtils.setFieldValue(obj, startTimeKey, getTime(timeRange.getStartTime()));
                ReflectionUtils.setFieldValue(obj, endTimeKey, getTime(timeRange.getEndTime()));
            }
        }else if(startTimeEL.indexOf('[') > -1 ){
            String key = startTimeEL.substring(0, startTimeEL.indexOf('['));
            startTimeKey = startTimeEL.substring(startTimeEL.indexOf('[')+1, startTimeEL.indexOf(']'));
            endTimeKey = endTimeEL.substring(endTimeEL.indexOf('[')+1, endTimeEL.indexOf(']'));
            if(parameterObject.containsKey(key) && parameterObject.get(key) instanceof Map){
                Map<String, Object> map = (Map<String, Object>) parameterObject.get(key);
                putToMapIfExists(map, startTimeKey, timeRange.getStartTime());
                putToMapIfExists(map, endTimeKey, timeRange.getEndTime());
            }
        }
        putToMapIfExists(parameterObject, startTimeKey, timeRange.getStartTime());
        putToMapIfExists(parameterObject, endTimeKey, timeRange.getEndTime());
    }

    private void putToMapIfExists(Map<String, Object> map, String key, TimeWrapper timeWrapper) {
        if (map.containsKey(key)) {
            map.put(key, getTime(timeWrapper));
        }
    }

    private Object getTime(TimeWrapper timeWrapper) {
        if(DateType.DATE_STRING==timeWrapper.getDateType()) {
            return timeWrapper.getDateStr();
        }else {
            return timeWrapper.getDate();
        }
    }

    private boolean isCount(String cacheKey) {
        return cacheKey.endsWith("_Count");
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    private String getSql(String originalSql, TimeRange timeRange) {
        if(StringUtils.isEmpty(timeRange.getTableSuffix())) {
            return originalSql;
        }
        ShardPluginConfiguration shardPluginConfiguration = ApplicationContextUtils.getBean(ShardPluginConfiguration.class);
        String sql = originalSql;
        for(TableShardStrategyConfig config : shardPluginConfiguration.getTableShardStrategyList()) {
            Set<String> tableNames = config.getTableName();
            for(String tableName : tableNames) {
                if (sql.toUpperCase().contains(" " + tableName.toUpperCase() + " ")) {
                    String newTableName = tableName + timeRange.getTableSuffix();
                    sql = replaceTableName(sql, tableName, newTableName);
                }
            }
        }
        return sql;
    }

    private String replaceTableName(String input,String oldTableName,String newTableName){
        String regex = "[\\s*|\t|\r|\n]"+oldTableName+"[\\s*|\t|\r|\n]";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find();
        while (result)
        {
            m.appendReplacement(sb, " "+ newTableName+" ");
            result = m.find();
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public void setProperties(Properties properties) {
        //let this block empty.
    }
}