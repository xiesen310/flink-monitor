package com.zork.flink.monitor.init;

import com.zork.flink.monitor.model.Resp;
import com.zork.flink.monitor.service.StorageService;
import com.zork.flink.monitor.service.StreamMonitor;
import com.zork.flink.monitor.utils.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

/**
 * @author xiese
 * @Description 性能测试定时任务
 * @Email xiesen310@163.com
 * @Date 2020/7/5 16:53
 */
@Configuration
@EnableScheduling
public class PerformanceTestTask {
    private final Logger logger = LoggerFactory.getLogger(PerformanceTestTask.class);

    @Autowired
    private StreamMonitor streamMonitor;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private StorageService storageService;

    /**
     * 获取监控数据，将监控数据写入到 influx database
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void initTaskMap() {
        storageService.tpsStoreInflux();
        taskMonitor();
    }

    /**
     * 监控任务状态，当数据量达到期望值的时候，重新启动一下任务
     */
    private void taskMonitor() {
        Resp taskList = streamMonitor.getTaskList();
        if (null != taskList.getData()) {
            Set<String> data = (Set<String>) taskList.getData();
            if (data.size() > 0) {
                for (String key : data) {
                    Resp resp = streamMonitor.noSqlPerformanceTest(key);
                }
            } else {
                logger.warn("当前集群无运行任务,启动一个新任务");
                String taskName = appProperties.getPerformanceTestTaskName();
                streamMonitor.startJob(taskName);
            }
        }
    }

}
