package com.zork.flink.monitor.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiese
 * @Description 配置文件
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:32
 */
@Configuration
@Getter
public class AppProperties {
    @Value("${flink.master.url}")
    private String flinkMasterUrl;

    @Value("${influx.db.url}")
    private String influxDatabaseUrl;

    @Value("${influx.db.database}")
    private String influxDatabaseName;

    @Value("${kafka.source.expect.records.num}")
    private String expectRecordsNum;

    @Value("${streamx.url}")
    private String streamxUrl;

    @Value("${performance.test.task.name}")
    private String performanceTestTaskName;
}
