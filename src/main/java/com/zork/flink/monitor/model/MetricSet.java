package com.zork.flink.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * @author xiese
 * @Description 指标存储 model
 * @Email xiesen310@163.com
 * @Date 2020/7/5 12:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MetricSet {
    String metricSetName;
    String timestamp;
    Map<String, String> dimensions;
    Map<String, Double> metrics;
}
