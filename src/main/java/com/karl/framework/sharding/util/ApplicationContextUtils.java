package com.karl.framework.sharding.util;


import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author karl.zhong
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {
    private static ApplicationContext context = null;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtils.context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return ApplicationContextUtils.context.getBean(clazz);
    }
}
