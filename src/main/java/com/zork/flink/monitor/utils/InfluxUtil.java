package com.zork.flink.monitor.utils;

import com.zork.flink.monitor.model.MetricSet;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author xiese
 * @Description influx database 工具类
 * @Email xiesen310@163.com
 * @Date 2020/7/5 12:08
 */
public class InfluxUtil {
    private String url = "";
    private String username = "";
    private String password = "";
    private String database = "";
    private String policyName = "";
    Boolean enableGzip = false;
    Boolean enableCreateDatabase = false;
    private InfluxDB influxdb = null;

    public InfluxUtil(String url) {
        this.url = url;
    }

    /**
     * 设置数据库
     */
    public InfluxUtil setDatabase(String database, String policyName) {
        this.database = database;
        this.policyName = policyName;
        return this;
    }

    public InfluxUtil setAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public InfluxUtil setCompression(Boolean enableGzip, Boolean enableCreateDatabase) {
        this.enableGzip = enableGzip;
        this.enableCreateDatabase = enableCreateDatabase;
        return this;
    }

    public InfluxUtil build() {
        if (influxdb == null) {
            synchronized (this) {
                if (influxdb == null) {
                    influxdb = InfluxDBFactory.connect(url, username, password);
                }
            }
        }
        influxdb.createDatabase(database);
        return this;
    }

    /**
     * 插入数据
     */
    public void insert(String measurement, Map<String, String> tagsToAdd, Map<String, Object> fields, Long timestamp) {

        Point.Builder builder = Point.measurement(measurement).tag(tagsToAdd);
        if (fields != null && !fields.isEmpty()) {
            builder.fields(fields);
        }
        if (timestamp != 0L) {
            builder.time(timestamp, TimeUnit.MILLISECONDS);
        }
        influxdb.write(database, policyName, builder.build());
    }


    /**
     * 插入批量插入数据
     *
     * @param metrics
     * @throws InterruptedException
     */
    public void batchInsert(BlockingQueue<MetricSet> metrics) throws InterruptedException {
        int size = metrics.size();
        BatchPoints batchPoints = BatchPoints
                .database(database)
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        for (int i = 0; i < size; i++) {
            MetricSet metric = metrics.poll(3, TimeUnit.SECONDS);
            Point.Builder builder = Point.measurement(metric.getMetricSetName()).tag(metric.getDimensions());
            Map<String, Double> tempMap = new HashMap<>(20);
            if (metric.getMetrics() != null && !metric.getMetrics().isEmpty()) {
                builder.fields(Collections.<String, Object>unmodifiableMap(metric.getMetrics()));
            }
            Long timestamp = getMsTime(metric.getTimestamp());
            builder.time(timestamp, TimeUnit.MILLISECONDS);

            batchPoints.point(builder.build());
        }
        influxdb.write(batchPoints);
    }

    /**
     * 转换时间
     *
     * @param str 时间格式字符串
     * @return long
     */
    public static long getMsTime(String str) {
        String one = "1";
        int ten = 10;
        int thirteen = 13;
        if (str == null) {
            return -1;
        }
        str = str.trim();
        try {
            // 豪秒数，1能管到2033年
            if (str.length() == thirteen && str.startsWith(one)) {
                return Long.parseLong(str);
            }
            // 秒数，1能管到2033年
            else if (str.length() == ten && str.startsWith(one)) {
                return Long.parseLong(str) * 1000;
            } else {
                // datetime = new DateTime("2033-02-13T00:37:38.778Z");
                // datetime = new DateTime("2017-02-23T00:37:38.778+08:00");
                return new Date(str).getTime();
            }
        } catch (Exception ex) {
            return -1;
        }
    }


    /**
     * 查询数据
     */
    public QueryResult query(String command) {
        return influxdb.query(new Query(command, database));
    }
}
