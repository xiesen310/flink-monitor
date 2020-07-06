package com.zork.flink.monitor.service;

import com.zork.flink.monitor.model.Resp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * @author xiese
 * @Description TODO
 * @Email xiesen310@163.com
 * @Date 2020/7/5 13:13
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageServiceTest {

    @Autowired
    private StorageService storageService;

    @Autowired
    private StreamMonitor streamMonitor;

    @Test
    public void tpsStoreInflux() {
        storageService.tpsStoreInflux();
        System.out.println("test");
    }

    @Test
    public void cancelDataStoreInflux() {
        Resp taskList = streamMonitor.getTaskList();
        if (null != taskList.getData()) {
            Set<String> data = (Set<String>) taskList.getData();
            if (data.size() > 0) {
                for (String key : data) {
                    storageService.cancelDataStoreInflux(key);
                }
            } else {
                System.out.println("当前集群无运行任务");
            }
        }

    }
}
