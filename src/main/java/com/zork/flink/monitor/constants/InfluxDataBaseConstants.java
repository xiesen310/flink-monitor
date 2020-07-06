package com.zork.flink.monitor.constants;

/**
 * @author xiese
 * @Description InfluxDataBaseConstants
 * @Email xiesen310@163.com
 * @Date 2020/7/5 12:18
 */
public interface InfluxDataBaseConstants {
    String INFLUX_DB_URL = "influx.db.url";
    String INFLUX_DB_USERNAME = "influx.db.username";
    String INFLUX_DB_PASSWORD = "influx.db.password";
    String INFLUX_DB_DATABASE = "influx.db.database";
    String INFLUX_DB_BATCH_ACTIONS = "influx.db.batchActions";
    String INFLUX_DB_FLUSH_DURATION = "influx.db.flushDuration";
    String INFLUX_DB_FLUSH_DURATIONTIMEUNIT = "influx.db.flushDurationTimeUnit";
    String INFLUX_DB_ENABLE_GZIP = "influx.db.enableGzip";
    String INFLUX_DB_CREATE_DATABASE = "influx.db.createDatabase";
}
