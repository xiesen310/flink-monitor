package com.zork.flink.monitor.init;

import com.zork.flink.monitor.model.Resp;
import com.zork.flink.monitor.service.StreamMonitor;
import com.zork.flink.monitor.utils.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author xiese
 * @Description 项目启动成功之后加载
 * @Email xiesen310@163.com
 * @Date 2020/7/5 23:10
 */
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(ApplicationRunnerImpl.class);

    @Autowired
    private StreamMonitor streamMonitor;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private PerformanceTestTask performanceTestTask;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("项目启动成功,清理一下 flink 集群 [{}]上的任务", appProperties.getFlinkMasterUrl());
        clean();
//        performanceTestTask.initTaskMap();
    }

    /**
     * 清理集群上的任务，然后启动一个新的任务
     */
    private void clean() {
        Resp taskList = streamMonitor.getTaskList();
        if (null != taskList.getData()) {
            Set<String> data = (Set<String>) taskList.getData();
            for (String id : data) {
                logger.info("清理集群 [{}] 上的任务,id为: {}", appProperties.getFlinkMasterUrl(), id);
                streamMonitor.cancelJob(id);
            }

            streamMonitor.startJob(appProperties.getPerformanceTestTaskName());
        }
    }
}
