package com.zork.flink.monitor.utils;

import com.zork.flink.monitor.model.MetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

/**
 * @author xiese
 * @Description MetricSetStore
 * @Email xiesen310@163.com
 * @Date 2020/7/5 12:14
 */
public class MetricSetStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricSetStore.class);

    /**
     * 将指标数据存储到 influxDB
     *
     * @param url      influxDB地址: http://192.168.1.91:8086
     * @param database 数据库名称
     * @param metrics  指标列表数据
     * @throws IOException
     * @throws InterruptedException
     */
    public static void storeMetricsList2InfluxDatabase(String url, String database, BlockingQueue<MetricSet> metrics) {
        String username = "admin";
        String password = "admin";
        String policyName = "autogen";
        Boolean enableGzip = false;
        Boolean enableCreateDatabase = false;
        storeMetricsList2InfluxDatabase(url, database, username, password, policyName, enableGzip, enableCreateDatabase, metrics);
    }

    /**
     * 将指标数据存储到 influxDB
     *
     * @param url                  influxDB地址: http://192.168.1.91:8086
     * @param database             数据库名称
     * @param username             用户名
     * @param password             密码
     * @param policyName           保存策略
     * @param enableGzip           是否进行 Gzip 压缩
     * @param enableCreateDatabase 是否允许创建数据库
     * @param metrics              指标列表数据
     * @throws IOException
     * @throws InterruptedException
     */
    public static void storeMetricsList2InfluxDatabase(String url,
                                                       String database,
                                                       String username,
                                                       String password,
                                                       String policyName,
                                                       Boolean enableGzip,
                                                       Boolean enableCreateDatabase,
                                                       BlockingQueue<MetricSet> metrics
    ) {

        try {
            int size = metrics.size();
            if (size > 0) {
                InfluxUtil svr = new InfluxUtil(url)
                        .setDatabase(database, policyName)
                        .setAuthentication(username, password)
                        .setCompression(enableGzip, enableCreateDatabase)
                        .build();
                svr.batchInsert(metrics);
            }
        } catch (Exception e) {
            LOGGER.warn(getTime() + "error ：InfluxDB出错  ：", e);
        }
    }

    private static ThreadLocal<SimpleDateFormat> sdf3 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss] ");
        }
    };

    public static String getTime() {
        return sdf3.get().format(new Date());
    }
}
