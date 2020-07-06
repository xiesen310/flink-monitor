package com.zork.flink.monitor.utils;

import com.zork.flink.monitor.model.MetricSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiese
 * @Description TODO
 * @Email xiesen310@163.com
 * @Date 2020/7/5 12:41
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MetricSetStoreTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    public void storeMetricsList2InfluxDatabase() {
        String url = appProperties.getInfluxDatabaseUrl();
        String database = appProperties.getInfluxDatabaseName();
        BlockingQueue<MetricSet> genericRecordQueue = new LinkedBlockingQueue<MetricSet>();

        MetricSet metricSet = new MetricSet();
        String metricSetName = "test";
        String timestamp = System.currentTimeMillis() + "";
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("name", "xiesen");

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("age", 13.0);

        metricSet.setMetricSetName(metricSetName);
        metricSet.setTimestamp(timestamp);
        metricSet.setDimensions(dimensions);
        metricSet.setMetrics(metrics);
        genericRecordQueue.add(metricSet);

        MetricSetStore.storeMetricsList2InfluxDatabase(url, database, genericRecordQueue);
    }
}
