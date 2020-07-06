package com.zork.flink.monitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zork.flink.monitor.model.JobDetail;
import com.zork.flink.monitor.model.MetricSet;
import com.zork.flink.monitor.model.Resp;
import com.zork.flink.monitor.service.StorageService;
import com.zork.flink.monitor.service.StreamMonitor;
import com.zork.flink.monitor.utils.AppProperties;
import com.zork.flink.monitor.utils.DateUtil;
import com.zork.flink.monitor.utils.MetricSetStore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiese
 * @Description 存储服务实现类
 * @Email xiesen310@163.com
 * @Date 2020/7/5 12:26
 */
@Service
public class StorageServiceImpl implements StorageService {
    private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Autowired
    private StreamMonitor streamMonitor;

    @Autowired
    private AppProperties appProperties;

    @Override
    public void tpsStoreInflux() {
        String url = appProperties.getInfluxDatabaseUrl();
        String databaseName = appProperties.getInfluxDatabaseName();
        BlockingQueue<MetricSet> genericRecordQueue = new LinkedBlockingQueue<MetricSet>();
        Resp taskList = null;
        try {
            taskList = streamMonitor.getTaskList();
        } catch (Exception e) {
            logger.error("获取集群 {} 任务失败,{}", url, e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            if (null != taskList.getData()) {
                Set<String> data = (Set<String>) taskList.getData();
                if (data.size() > 0) {
                    for (String key : data) {
                        Resp resp = streamMonitor.getNoSqlTaskTps(key);
                        if (null != resp.getData()) {
                            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(resp.getData()));
                            String jobId = String.valueOf(jsonObject.get("jobId"));
                            String timestamp = String.valueOf(jsonObject.get("ts"));
                            String metricSetName = "tps_" + jobId;
                            Set<String> stringSet = jsonObject.keySet();

                            for (String str : stringSet) {
                                boolean conditions = str.equals("ts") || str.equals("jobId") || str.equals("state");
                                if (!conditions) {
                                    JSONObject parseObject = JSONObject.parseObject(JSON.toJSONString(jsonObject.get(str)));
                                    String subTaskName = String.valueOf(parseObject.get("subTaskName"));
                                    String subTaskId = String.valueOf(parseObject.get("subTaskId"));
                                    String parallelism = String.valueOf(parseObject.get("parallelism"));
                                    JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(parseObject.get("data")));
                                    Map<String, Double> metrics = new HashMap<>();
                                    Map<String, String> dimensions = new HashMap<>();
                                    dimensions.put("jobId", jobId);
                                    dimensions.put("subTaskName", subTaskName);
                                    dimensions.put("subTaskId", subTaskId);
                                    dimensions.put("parallelism", parallelism);

                                    for (Object object : jsonArray) {
                                        JSONObject jsonObject1 = JSONObject.parseObject(JSON.toJSONString(object));
                                        String id = String.valueOf(jsonObject1.get("id"));
                                        String tmpId = null;
                                        // 获取的指标数据的key是以数字开头的，在写入到 influxdb 中，查询不了，在这里做转换，将分区编号，写到最后面
                                        if (StringUtils.isNotEmpty(id)) {
                                            String[] ids = id.split("\\.");
                                            tmpId = ids[1] + "_" + ids[0];
                                        }
                                        Double value = Double.valueOf(String.valueOf(jsonObject1.get("value")));
                                        metrics.put(tmpId, value);
                                    }
                                    genericRecordQueue.add(new MetricSet(metricSetName, timestamp, dimensions, metrics));
                                }
                            }

                            try {
                                MetricSetStore.storeMetricsList2InfluxDatabase(url, databaseName, genericRecordQueue);
                            } catch (Exception e) {
                                logger.error("指标数据写入 influx database 失败, influx database url address is [{}],influx database name is [{}] {}",
                                        url, databaseName, e.getMessage());
                                e.printStackTrace();
                                return;
                            }

                        }
                    }
                } else {
                    logger.warn("集群 {} 无运行的任务", appProperties.getFlinkMasterUrl());
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("解析 tps 数据异常, {}", e.getMessage());
            e.printStackTrace();
            return;
        }

    }

    @Override
    public void cancelDataStoreInflux(String id) {
        Resp resp = streamMonitor.findTaskById(id);
        String url = appProperties.getInfluxDatabaseUrl();
        String databaseName = appProperties.getInfluxDatabaseName();
        if (null != resp.getData()) {
            JobDetail data = (JobDetail) resp.getData();
            Long duration = data.getDuration();
            Long lastModification = data.getLastModification();

            String jid = data.getJid();
            String jobName = data.getName();
            Long startTime = data.getStartTime();

            BlockingQueue<MetricSet> genericRecordQueue = new LinkedBlockingQueue<MetricSet>();
            String metricSetName = "cancel_record";
            String timestamp = String.valueOf(System.currentTimeMillis());
            Map<String, String> dimensions = new HashMap<>();
            dimensions.put("jid", jid);
            dimensions.put("jobName", jobName);
            dimensions.put("startTime", DateUtil.timestampToDate(startTime));
            dimensions.put("lastModification", DateUtil.timestampToDate(lastModification));
            dimensions.put("runTime", DateUtil.formatDateTime(duration));
            Map<String, Double> metrics = new HashMap<>();
            metrics.put("duration", Double.valueOf(duration));
            genericRecordQueue.add(new MetricSet(metricSetName, timestamp, dimensions, metrics));

            try {
                MetricSetStore.storeMetricsList2InfluxDatabase(url, databaseName, genericRecordQueue);
            } catch (Exception e) {
                logger.error("指标数据写入 influx database 失败, influx database url address is [{}],influx database name is [{}] {}",
                        url, databaseName, e.getMessage());
                e.printStackTrace();
                return;
            }
            streamMonitor.cancelJob(id);

        }
    }
}
