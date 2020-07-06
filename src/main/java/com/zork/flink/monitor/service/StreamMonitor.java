package com.zork.flink.monitor.service;

import com.zork.flink.monitor.model.Resp;

public interface StreamMonitor {
    /**
     * 获取集群配置信息
     *
     * @return
     */
    Resp getClusterConfig();


    /**
     * 获取运行的任务列表
     *
     * @return
     */
    Resp getTaskList();

    /**
     * 根据 任务 id 获取任务详情
     *
     * @param id
     * @return
     */
    Resp findTaskById(String id);


    /**
     * 获取 sql 任务 tps
     *
     * @param id
     * @return
     */
    Resp getSqlTaskTps(String id);


    /**
     * 获取 非 sql 任务 tps
     *
     * @param id
     * @return
     */
    Resp getNoSqlTaskTps(String id);

    /**
     * 获取 kafka source 数据的总记录数
     *
     * @param id
     * @return
     */
    Resp getNoSqlSourceRecords(String id);

    /**
     * 退出任务
     *
     * @param id
     * @return
     */
    boolean cancelJob(String id);

    Resp startJob(String confName);

    /**
     * no sql 性能测试
     *
     * @param id
     * @return
     */
    Resp noSqlPerformanceTest(String id);


}
