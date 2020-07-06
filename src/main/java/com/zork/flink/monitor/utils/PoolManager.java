package com.zork.flink.monitor.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * @author xiese
 * @Description http 线程池
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:18
 */
public class PoolManager {
    private static PoolingHttpClientConnectionManager clientConnectionManager = null;
    private static int defaultMaxTotal = 200;
    private static int defaultMaxPerRoute = 25;

    public static CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpclient;
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).build();
        if (clientConnectionManager == null) {
            clientConnectionManager = new PoolingHttpClientConnectionManager();
            clientConnectionManager.setMaxTotal(defaultMaxTotal);
            clientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        }
        httpclient = HttpClients.custom().setConnectionManager(clientConnectionManager)
                .setConnectionManagerShared(true)
                .setDefaultRequestConfig(requestConfig).build();
        return httpclient;
    }
}
