# sharding-support介绍
sharding-support目前提供基于mybatis的按时间分表的支持。

## 主要功能说明
1. 支持按时间进行分配
2. 支持按正则表达式指定mapperId是否需要进行分表，支持部分mapperId进行执行分表策略，部分mapperId不执行分表策略。
3. 支持多个表使用同一个分表策略。
4. 支持可配置的分表规则，一年的时间可以配置多个分表，分表最大时间范围为1年，最小范围为1个月。
5. 支持自定义分表策略。

## 注意：
1. mybatis 需要在3.4以上
2. mybatis-spring 2.0.1以上
3. @TimeShardQuery不能在抽象方法上。
4. Dao层上的方法需要包含 java.util.Date类型的两个参数，“开始时间”与“结束时间”参数名不做要求，才能进行分表。

## maven添加依赖
```xml
<dependency>
  <groupId>com.karl.framework</groupId>
  <artifactId>sharding-support</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## mybatis-config.xml中添加plugin
```xml
<plugin interceptor="com.karl.framework.sharding.plugin.ShardPlugin" />
```

## spring bean 配置
增加如下bean配置， 下面是几个案例。

### 配置哪些表需要采用分表策略
```xml
<bean id="time_range_shard_strategy_config" class="com.karl.framework.sharding.plugin.config.TableShardStrategyConfig">
        <constructor-arg name="tableNames">
            <set>
                <value>oms_tio</value>
                <value>oms_orderinfo</value>
            </set>
        </constructor-arg>
        <constructor-arg name="strategy" ref="monthArchiveShardStrategy" />
    </bean>

```
### 配置分表策略， 注意需要覆盖TimeDuration需要覆盖一整年。fromDate与toDate 是 前闭后开，如下的区间为[11-2, 5-6), [5-6,11-2)两个区间。
```xml
<bean id="monthArchiveShardStrategy" class="com.karl.framework.sharding.strategy.ma.MonthArchiveShardStrategy" >
       <!--keepArchiveTableNum :同一个表保留归档表的个数
       保留的归档表数量, 比如一年有4个归档表，然后需要保留5年的数据，则keepArchiveTableNum 应该设置为5*4=20;
       如果不是根据时间查询时，则需要进行扫表，查查询的是单条数据时，则查到了就返回，如果查询是集合，则需要查询所有的归档表，每个归档表都需要查询一次。
       -->
        <constructor-arg name="keepArchiveTableNum" value="4"></constructor-arg>
        <constructor-arg name="configTimeDurations">
            <list>
                <bean class="com.karl.framework.sharding.strategy.ma.TimeDuration">
                    <constructor-arg name="fromDate" value="11-1"></constructor-arg>
                    <constructor-arg name="toDate" value="5-1"></constructor-arg>
                    <constructor-arg name="index" value="0"></constructor-arg>
                </bean>
                <bean class="com.karl.framework.sharding.strategy.ma.TimeDuration">
                    <constructor-arg name="fromDate" value="5-1"></constructor-arg>
                    <constructor-arg name="toDate" value="11-1"></constructor-arg>
                    <constructor-arg name="index" value="1"></constructor-arg>
                </bean>
            </list>
        </constructor-arg>
    </bean>
```
### ShardPluginConfiguration 指定哪些mybatis中的mapperId需要采用分表，采用黑白名单的方式。
```xml
<bean class="com.karl.framework.sharding.plugin.config.ShardPluginConfiguration" >
        <property name="tableShardStrategyList">
            <list>
                <ref bean="time_range_shard_strategy_config" />
            </list>
        </property>
        <property name="whiteListMapperIdRegex">
            <set>
                <value>.*.dao.UserDAO.*</value>
                <value>.*.dao.OrderDao.*</value>
            </set>
        </property>
        <property name="blackListMapperIdRegex">
            <set>
                <value>com.karl.framework.sharding.dao.UserDAO.add</value>
            </set>
        </property>
    </bean>
```
## Dao层方法上需要增加annotation   
### @TimeShardQuery 带时间范围的查询
```java
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
    String startTime() default Contants.NO_START_TIME;
    /**
     * format yyyy-MM-dd
     * 结束时间，使用EL表达式从参数中取数
     * 没有结束时间，则当前时间为默认的结束时间。
     * @return
     */
    String endTime() default Contants.NO_END_TIME;

    /**
     * 是否为count查询。
     * @return
     */
    boolean isCountQuery() default  false;

    /**
     * 查询数量，分页是需要用， 使用EL表达式从参数中取数
     * @return
     */
    String limit() default Contants.NO_LIMITED;
    
    /**
     * 按时间的排序规则。默认是顺序，目前有如下两种选择
     * OrderStrategy.ASC
     * OrderStrategy.DESC
     * OrderStrategy.OTHER 赞不支持
     * @return
     */
    OrderStrategy orderStrategy() default OrderStrategy.TIME_ASC;
}
```

### TimeShardQuery 中EL表达式说明
| 序号 | 参数类型 | 参数名 |属性名|表达式|
| ------ | ------ | ------ |------ |------ |
| 1 | 简单参数 | paramName |~|#paramName|
| 2 | Map | map |startTime|#map[startTime]|
| 3 | 复杂对象 | obj |startTime|#obj.startTime|

```java
@TimeShardQuery(startTime = "#startTime", endTime = "#endTime", limit = "#pageSize")
public List<Order> findByCreTime(Date startTime, Date endTime, int startIndex, int pageSize) {
    return orderDao.findByCreTime(startTime, endTime, startIndex, pageSize);
}

@TimeShardQuery(startTime = "#map[startTime]", endTime = "#map[endTime]", isCountQuery = true)
public Integer getOrderCount(Map<String, Object> map) {
    return orderDao.getOrderCount(map);
}
 @TimeShardQuery(startTime = "#map.startTime", endTime = "#map.endTime", isCountQuery = true)
public Integer getOrderCount(OrderQuery query) {
    return orderDao.getOrderCount(query);
}
```