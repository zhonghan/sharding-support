package com.karl.framework.sharding.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *  一年做两次数据迁移，时间分别是5月1号0点和11月1号0点
 * @author karl.zhong
 */
public class DateUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private DateUtils() {}
    public static Date trim2Date(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String dateStr = sdf.format(date);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateStr));
            return cal.getTime();
        } catch (ParseException e) {
            throw new RuntimeException("date["+date+"] format error", e);
        }
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    /**
     *
     * @param currentYear
     * @param month 1 - 12
     * @param day
     * @return
     */
    public static Date createDateWithoutTime(int currentYear, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DATE, day);
        return DateUtils.trim2Date(cal.getTime());
    }

    public static boolean inSameYear(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        calendar.setTime(date2);
        int year2 = calendar.get(Calendar.YEAR);
        return year1 == year2;
    }

    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, DATE_FORMAT);
    }
    public static Date parseDate(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("date["+dateStr+"] parseDate error", e);
        }
    }

    public static String format(Date date) {
        if(date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        return sdf.format(date);
    }
}
