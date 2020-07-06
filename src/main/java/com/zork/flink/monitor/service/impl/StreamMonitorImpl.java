package com.zork.flink.monitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zork.flink.monitor.model.JobDetail;
import com.zork.flink.monitor.model.Resp;
import com.zork.flink.monitor.service.StorageService;
import com.zork.flink.monitor.service.StreamMonitor;
import com.zork.flink.monitor.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiese
 * @Description flink 流监控
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:31
 */
@Service
public class StreamMonitorImpl implements StreamMonitor {
    private final Logger logger = LoggerFactory.getLogger(StreamMonitorImpl.class);
    private final String RUNNING_STATUS = "RUNNING";

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private StorageService storageService;

    Set<String> sets = new HashSet<>();

    @Override
    public Resp getClusterConfig() {
        String result = null;
        String masterUrl = appProperties.getFlinkMasterUrl();
        try {
            result = HttpClientUtil.sendHttpGet(masterUrl + "/config");
        } catch (Exception e) {
            logger.error("获取集群配置信息失败: {}", e.getMessage());
            e.printStackTrace();
        }

        return RespHelper.ok(JSON.parseObject(result));
    }

    @Override
    public Resp getTaskList() {
        initCache();
        return RespHelper.ok(sets);
    }

    @Override
    public Resp findTaskById(String id) {
        JSONObject jsonObject = getTaskMonitorInfo(id);
        JobDetail jobDetail = null;
        String state = jsonObject.getString("state");
        if (RUNNING_STATUS.equals(state)) {
            String jid = jsonObject.getString("jid");
            String name = jsonObject.getString("name");
            Long startTime = jsonObject.getLong("start-time");
            Long endTime = jsonObject.getLong("end-time");
            Long duration = jsonObject.getLong("duration");
            Long lastModification = jsonObject.getLong("last-modification");
            if (lastModification == null) {
                lastModification = System.currentTimeMillis();
            }
            jobDetail = new JobDetail(jid, name, state, startTime, endTime, duration, lastModification);
        }
        return RespHelper.ok(jobDetail);
    }

    @Override
    public Resp getSqlTaskTps(String id) {
        String masterUrl = appProperties.getFlinkMasterUrl();
        String url = masterUrl + "/jobs/" + id;
        String monitorMsg = null;
        String result = null;
        String vid = null;
        String params = null;
        String ts = null;

        try {
            monitorMsg = HttpClientUtil.sendHttpGet(url);
        } catch (Exception e) {
            logger.error("获取监控信息失败,{}", e.getMessage());
            e.printStackTrace();
            return RespHelper.fail(-1, "获取监控信息失败, " + e.getMessage());
        }

        try {
            JSONObject jsonObject = JSON.parseObject(monitorMsg);
            /// 获取 flink 监控的采集时间
            Long now = Long.valueOf(String.valueOf(jsonObject.get("now")));
            ts = DateUtil.timestampToDate(now);

            vid = String.valueOf(JSON.parseObject(String.valueOf(jsonObject.getJSONArray("vertices").get(0))).get("id"));
            String name = String.valueOf(JSON.parseObject(String.valueOf(jsonObject.getJSONArray("vertices").get(0))).get("name"));
            Integer parallelism = (Integer) JSON.parseObject(String.valueOf(jsonObject.getJSONArray("vertices").get(0))).get("parallelism");

            /// 根据 parallelism 遍历每个分区的指标数据
            StringBuilder builder = new StringBuilder();
            name = name.replace(" ", "").replace(":", "__");
            String metricName = "dtNumRecordsInRate";
            String suffix = name + "." + metricName + ",";
            for (int i = 0; i < parallelism; i++) {
                builder.append(i + "." + suffix);
            }
            params = builder.toString().substring(0, builder.toString().length() - 1);
        } catch (Exception e) {
            logger.error("解析监控指标数据出错,{}", e.getMessage());
            e.printStackTrace();
            return RespHelper.fail(-1, "解析监控指标数据出错, " + e.getMessage());
        }

        String url1 = url + "/vertices/" + vid + "/metrics?get=" + params;
        try {
            result = HttpClientUtil.sendHttpGet(url1);
            return RespHelper.ok(ts + " " + result);
        } catch (Exception e) {
            logger.error("获取 dtNumRecordsInRate 指标信息失败, {}", e.getMessage());
            e.printStackTrace();
            return RespHelper.fail(-1, "获取 dtNumRecordsInRate 指标信息失败, " + e.getMessage());
        }
    }

    @Override
    public Resp getNoSqlTaskTps(String id) {
        JSONObject monitorInfo = getTaskMonitorInfo(id);
        String metricName = "numRecordsOutPerSecond";

        if (monitorInfo != null) {
            String jobId = String.valueOf(monitorInfo.get("jid"));
            String state = String.valueOf(monitorInfo.get("state"));
            Long now = Long.valueOf(String.valueOf(monitorInfo.get("now")));
            JSONArray vertices = JSONArray.parseArray(String.valueOf(monitorInfo.get("vertices")));
            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("jobId", jobId);
            mapResult.put("state", state);
            mapResult.put("ts", now);

            try {
                for (Object obj : vertices) {
                    Map<String, Object> map1 = new HashMap<>();
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(obj));
                    String subTaskId = String.valueOf(jsonObject.get("id"));
                    String subTaskName = String.valueOf(jsonObject.get("name"));
                    Integer parallelism = Integer.valueOf(String.valueOf(jsonObject.get("parallelism")));
                    map1.put("subTaskId", subTaskId);
                    map1.put("subTaskName", subTaskName);
                    map1.put("parallelism", parallelism);

                    StringBuilder builder = new StringBuilder();

                    String suffix = metricName + ",";
                    for (int i = 0; i < parallelism; i++) {
                        builder.append(i + "." + suffix);
                    }
                    String params = builder.toString().substring(0, builder.toString().length() - 1);
                    String url1 = appProperties.getFlinkMasterUrl() + "/jobs/" + id + "/vertices/" + subTaskId + "/metrics?get=" + params;

                    try {
                        String result = HttpClientUtil.sendHttpGet(url1);
                        if (result != null) {
                            map1.put("data", JSONArray.parseArray(result));
                        }
                        mapResult.put(subTaskId, map1);
                    } catch (Exception e) {
                        logger.error("获取 [{}] 指标信息失败, {}", metricName, e.getMessage());
                        e.printStackTrace();
                        return RespHelper.fail(-1, "获取 [" + metricName + "] 指标信息失败, " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("解析指标 [{}] 数据异常, {}", metricName, e.getMessage());
                e.printStackTrace();
                return RespHelper.fail(-1, "解析指标 [" + metricName + "] 数据异常," + e.getMessage());
            }

            return RespHelper.ok(mapResult);
        } else {
            logger.error("获取 [" + metricName + "] 监控数据失败");
            return RespHelper.fail(-1, "获取 [" + metricName + "] 监控数据失败");
        }
    }

    /**
     * 获取基本监控信息
     *
     * @param id
     * @return
     */
    private JSONObject getTaskMonitorInfo(String id) {
        String masterUrl = appProperties.getFlinkMasterUrl();
        String url = masterUrl + "/jobs/" + id;
        JSONObject jsonObject = null;
        try {
            String monitorMsg = HttpClientUtil.sendHttpGet(url);
            if (monitorMsg != null) {
                jsonObject = JSON.parseObject(monitorMsg);
            }
        } catch (Exception e) {
            logger.error("获取监控信息失败,{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    @Override
    public Resp getNoSqlSourceRecords(String id) {
        String url = appProperties.getFlinkMasterUrl() + "/jobs/" + id;
        String monitorMsg = null;

        try {
            monitorMsg = HttpClientUtil.sendHttpGet(url);
        } catch (Exception e) {
            logger.error("获取监控信息失败,{}", e.getMessage());
            e.printStackTrace();
            return RespHelper.fail(-1, "获取监控信息失败, " + e.getMessage());
        }
        Long writeRecords = 0L;
        try {
            JSONObject jsonObject = JSON.parseObject(monitorMsg);
            String name = String.valueOf(JSON.parseObject(String.valueOf(jsonObject.getJSONArray("vertices").get(0))).get("name"));
            if (name.contains("Source")) {
                String value = String.valueOf(JSONObject.parseObject(String.valueOf(JSON.parseObject(String.valueOf(jsonObject.getJSONArray("vertices").get(0))).get("metrics"))).get("write-records"));
                writeRecords = Long.valueOf(value);
            }
            return RespHelper.ok(writeRecords);
        } catch (Exception e) {
            logger.error("解析监控指标数据出错,{}", e.getMessage());
            e.printStackTrace();
            return RespHelper.fail(-1, "解析监控指标数据出错, " + e.getMessage());
        }
    }

    @Override
    public boolean cancelJob(String id) {
        String streamxUrl = appProperties.getStreamxUrl();
        String cancelUrl = streamxUrl + "/api/jobs/cancel/" + id;
        // 判断任务是否存在
        if (taskIsExist(id)) {
            try {
                String s = HttpClientUtil.sendHttpGet(cancelUrl);
                JSONObject jsonObject = JSONObject.parseObject(s);
                Integer code = Integer.valueOf(String.valueOf(jsonObject.get("code")));
                if (0 == code) {
                    return true;
                } else {
                    logger.error("在 [{}] 集群上,停止任务 [{}] 失败", appProperties.getFlinkMasterUrl(), id);
                    return false;
                }
            } catch (Exception e) {
                logger.error("请求 streamx 地址 [{}] 失败, {}", streamxUrl, e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            logger.warn("在 [{}] 集群上,任务 [{}] 不存在", appProperties.getFlinkMasterUrl(), id);
            return false;
        }
    }

    @Override
    public Resp startJob(String confName) {
        if (confName.endsWith(".conf")) {
//            String path = this.getClass().getResource("/streamx/" + confName).getPath();
            logger.info("提交任务读取配置文件路径: {}", confName);
            String s = ReadConfig.read(confName);
            String streamxUrl = appProperties.getStreamxUrl();
            String url = streamxUrl + "/api/jobs/submit";
            try {
                String s1 = HttpClientUtil.sendHttpPost(url, s);
                JSONObject jsonObject = JSONObject.parseObject(s1);
                if (Integer.parseInt(jsonObject.get("code").toString()) == 0) {
                    return RespHelper.ok(jsonObject.get("data"));
                } else {
                    return RespHelper.fail(-1, String.valueOf(jsonObject.get("message")));
                }
            } catch (Exception e) {
                logger.error("请求 streamx 失败, url: {}", url);
                e.printStackTrace();
                return RespHelper.fail(-1, "请求 streamx 提交任务失败,请求地址: " + url);
            }
        } else {
            logger.error("", ".conf");
            return RespHelper.fail(-1, "配置文件必须以 [.conf] 结尾");
        }
    }

    @Override
    public Resp noSqlPerformanceTest(String id) {
        String expectRecordsNum = appProperties.getExpectRecordsNum();
        Long expectNum = Long.valueOf(expectRecordsNum);
        Resp resp = getNoSqlSourceRecords(id);
        Long writeRecords = null;

        if (null != resp.getData()) {
            writeRecords = Long.valueOf(String.valueOf(resp.getData()));
        }

        if (writeRecords - expectNum >= 0) {
            System.out.println(id);
            // 杀掉任务
            storageService.cancelDataStoreInflux(id);
            return RespHelper.ok(writeRecords, "退出任务 [" + id + "] 成功");
        }

        return RespHelper.ok(writeRecords);
    }

    private boolean taskIsExist(String id) {
        Resp resp = findTaskById(id);
        return null == resp.getData() ? false : true;
    }

    private void initCache() {
        sets.clear();
        String result = null;
        String masterUrl = appProperties.getFlinkMasterUrl();
        try {
            result = HttpClientUtil.sendHttpGet(masterUrl + "/jobs/overview");
            JSONObject jsonObject = JSON.parseObject(result);
            String jobs = jsonObject.get("jobs").toString();
            JSONArray objects = JSON.parseArray(jobs);
            for (Object object : objects) {
                JSONObject jsonObj = JSONObject.parseObject(object.toString());
                String state = jsonObj.getString("state");
                if (RUNNING_STATUS.equals(state)) {
                    String jid = jsonObj.getString("jid");
                    sets.add(jid);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("初始化任务缓存,{}", JSON.toJSONString(sets));
            }
        } catch (Exception e) {
            logger.error("初始化任务缓存失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
