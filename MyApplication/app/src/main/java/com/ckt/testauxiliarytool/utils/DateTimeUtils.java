package com.ckt.testauxiliarytool.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wgp on 2017/8/31.
 * 处理日期和格式化日期的工具类
 */

public class DateTimeUtils {
    private static final String SHORT_YEAR_FORMAT = "yyyy-MM-dd HH:mm";// 采取年简短月简短，不显示秒
    private static final String DETAIL_FORMAT = "yyyy-MM-dd HH:mm:ss";// 带时间详情的格式方式2000-00-00
    private static final String DETAIL_LSH_FORMAT = "yyyyMMddHHmmss";// 带有详情的时间格式化方式
    private static final String PICTURE_FORMAT = "yyyyMMdd_HHmmss";
    private static SimpleDateFormat detailFormat = new SimpleDateFormat(DETAIL_FORMAT);
    private static SimpleDateFormat shortYearFormat = new SimpleDateFormat(SHORT_YEAR_FORMAT);
    private static SimpleDateFormat detailLSHFormat = new SimpleDateFormat(DETAIL_LSH_FORMAT);
    private static SimpleDateFormat pictureFormat = new SimpleDateFormat(PICTURE_FORMAT);

    /**
     * 返回日期的详细格式：yyyy-MM-dd HH:mm:ss
     *
     * @param date 传入一个日期
     * @return
     */
    public static String detailFormat(Date date) {
        if (date == null) {
            return "";
        } else {
            return detailFormat.format(date);
        }
    }

    /**
     * 返回两位数的年份的日期格式不显示秒，主要用于当两个时间相差很大时显示15-12-3 15:25
     *
     * @param date
     * @return
     */
    public synchronized static String shortYearFormat(Date date) {
        if (date == null) {
            return "";
        } else {
            return shortYearFormat.format(date);
        }
    }

    /**
     * 返回详细的流水号中使用的日期格式:yyyyMMddHHmmss
     *
     * @param date
     * @return
     */
    public synchronized static String detailLSHFormat(Date date) {
        if (date == null) {
            return "";
        } else {
            return detailLSHFormat.format(date);
        }
    }

    /**
     * 返回图片日期格式:yyyyMMdd_HHmmss
     */
    public synchronized static String detailPictureFormat() {
        return pictureFormat.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 使用默认格式将毫秒时间戳转换为字符串时间
     *
     * @param millis 毫秒时间戳
     * @return 格式化后的字符串时间
     */
    public static String millis2String(final long millis) {
        return millis2String(millis, detailFormat);
    }

    /**
     * 指定格式类型来将毫秒时间戳转换为时间字符串
     *
     * @param millis 毫秒时间戳
     * @param format 格式
     * @return 转换后的时间字符串
     */
    public static String millis2String(final long millis, final DateFormat format) {
        return format.format(new Date(millis));
    }

    /**
     * 使用默认格式将Date转换为时间字符串
     *
     * @param date 日期Date
     * @return 转换后的时间字符串
     */
    public static String date2String(final Date date) {
        return date2String(date, detailFormat);
    }

    /**
     * 使用指定格式将日期转换为时间字符串
     *
     * @param date   日期Date
     * @param format 指定的格式
     * @return 格式化之后的时间字符串
     */
    public static String date2String(final Date date, final DateFormat format) {
        return format.format(date);
    }

}
