package com.zork.flink.monitor.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xiese
 * @Description 时间转换工具类
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:49
 */
public class DateUtil {
    /**
     * 时间戳转指定日期格式日期
     *
     * @param timestamp
     * @param dateType
     * @return
     */
    public static String timestampToDate(long timestamp, String dateType) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateType);
        String date = sdf.format(new Date(timestamp));
        return date;
    }

    /**
     * 获取当前时间并转换成yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param timestamp
     * @return
     */
    public static String timestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String date = sdf.format(new Date(timestamp));
        return date;
    }

    /**
     * 将 ms 转换成格式化的日期格式
     *
     * @param mss
     * @return
     */
    public static String formatDateTime(long mss) {
        String DateTimes = null;
        long days = mss / (60 * 60 * 24 * 1000);
        long hours = (mss % (60 * 60 * 24 * 1000)) / (60 * 60 * 1000);
        long minutes = (mss % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (mss % (60 * 1000)) / 1000;
        if (days > 0) {
            DateTimes = days + "d" + hours + "h" + minutes + "min" + seconds + "s";
        } else if (hours > 0) {
            DateTimes = hours + "h" + minutes + "min"
                    + seconds + "s";
        } else if (minutes > 0) {
            DateTimes = minutes + "min" + seconds + "s";
        } else {
            DateTimes = seconds + "s";
        }
        return DateTimes;
    }

}
