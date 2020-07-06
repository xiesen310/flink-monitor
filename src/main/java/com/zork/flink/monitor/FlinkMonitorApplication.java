package com.zork.flink.monitor;

import com.zork.flink.monitor.model.Resp;
import com.zork.flink.monitor.service.StreamMonitor;
import com.zork.flink.monitor.utils.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Set;

@SpringBootApplication
public class FlinkMonitorApplication {


    public static void main(String[] args) {
        SpringApplication.run(FlinkMonitorApplication.class, args);
    }
}
