package com.zork.flink.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiese
 * @Description 任务详情 model
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetail {
    private String jid;
    private String name;
    private String state;
    private Long startTime;
    private Long endTime;
    private Long duration;
    private Long lastModification;
}
