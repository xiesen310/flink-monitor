package com.zork.flink.monitor.service;

public interface StorageService {
    /**
     * tps 数据存储到 influx database
     */
    void tpsStoreInflux();

    /**
     * 将退出数据存储到 influx database
     */
    void cancelDataStoreInflux(String id);
}
