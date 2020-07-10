package com.zork.flink.monitor.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zork.flink.monitor.model.Resp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * @author xiese
 * @Description StreamMonitorTest
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:53
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StreamMonitorTest {
    @Autowired
    private StreamMonitor streamMonitor;

    private void printObject(Object o) {
        System.out.println(JSON.toJSONString(o, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat));
    }

    @Test
    public void getClusterConfig() {
        Resp resp = streamMonitor.getClusterConfig();
        printObject(resp);
    }

    @Test
    public void getTaskList() {
        Resp resp = streamMonitor.getTaskList();
        printObject(resp);
    }

    @Test
    public void findTaskById() {
        Resp taskList = streamMonitor.getTaskList();
        Set<String> data = (Set<String>) taskList.getData();
        if (data.size() > 0) {
            for (String key : data) {
                Resp resp = streamMonitor.findTaskById(key);
                printObject(resp);
                System.out.println("==============================");
            }
        }
    }


    @Test
    public void getSqlTaskTps() {

    }

    @Test
    public void getNoSqlTaskTps() {
        Resp taskList = streamMonitor.getTaskList();
        if (null != taskList.getData()) {
            Set<String> data = (Set<String>) taskList.getData();
            if (data.size() > 0) {
                for (String key : data) {
                    Resp resp = streamMonitor.getNoSqlTaskTps(key);
                    printObject(resp);
                }
            }
        }
    }


    @Test
    public void getNoSqlSourceRecords() {
        Resp taskList = streamMonitor.getTaskList();
        if (null != taskList.getData()) {
            Set<String> data = (Set<String>) taskList.getData();
            if (data.size() > 0) {
                for (String key : data) {
                    Resp resp = streamMonitor.getNoSqlSourceRecords(key);
                    printObject(resp);
                }
            } else {
                System.out.println("当前集群无运行任务");
            }
        }

    }

    @Test
    public void noSqlPerformanceTest() {
        Resp taskList = streamMonitor.getTaskList();
        if (null != taskList.getData()) {
            Set<String> data = (Set<String>) taskList.getData();
            if (data.size() > 0) {
                for (String key : data) {
                    Resp resp = streamMonitor.noSqlPerformanceTest(key);
                    printObject(resp);
                }
            } else {
                System.out.println("当前集群无运行任务");
            }
        }
    }

    @Test
    public void startJob() {
        Resp resp = streamMonitor.startJob("empty.conf");
        printObject(resp);

    }
}
