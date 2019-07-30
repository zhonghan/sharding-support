package com.karl.framework.sharding.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author karl.zhong
 */
public class ReflectionUtils {
    private ReflectionUtils() {}

    /**
     * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object value) {
        Field field = getDeclaredField(object, fieldName);
        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            //ignore this exception
        }
    }

    /**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
     */
    public static Object getFieldValue(final Object object, final String fieldName) {
        Field field = getDeclaredField(object, fieldName);
        makeAccessible(field);

        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            //ignore this exception.
        }
        return result;
    }


    /**
     * 循环向上转型,获取对象的DeclaredField.
     */
    protected static Field getField(final Object object, final String fieldName) {
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                //ignore this exception
            }
        }
        return null;
    }
    protected static Field getDeclaredField(final Object object, final String fieldName) {
        Field field = getField(object, fieldName);
        if(field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }

        return field;

    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     */
    protected static void makeAccessible(final Field field) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
    }

    public static boolean containsVariable(Object obj, String variable) {
        return getField(obj, variable) != null;
    }
}
