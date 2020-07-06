package com.zork.flink.monitor.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiese
 * @Description TODO
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:18
 */
public class HttpClientUtil {
    public static final int INT200 = 200;
    public static final String SUFFIX = "&";
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static int poolManagerMaxTotal = INT200;
    private static int poolManagerMaxPerRoute = 50;
    private static int socketTimeout = 3 * 10000;
    private static int connectTimeout = 3 * 10000;
    /**
     * 设置请求和传输超时时间
     */
    private static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(socketTimeout)
            .setConnectTimeout(connectTimeout)
            .setConnectionRequestTimeout(connectTimeout)
            // cookie warn
            .setCookieSpec(CookieSpecs.STANDARD)
            .build();

    public HttpClientUtil() {
    }

    /**
     * 获取httpclient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = PoolManager.getHttpClient();
        return httpClient;
    }

    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     */
    public static String sendHttpPost(String httpUrl) throws Exception {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        return sendHttpPost(httpPost);
    }

    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     * @param params  参数(格式:key1=value1&key2=value2)
     */
    public static String sendHttpPost(String httpUrl, String params) throws Exception {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        // 设置参数
        StringEntity stringEntity = new StringEntity(params, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        return sendHttpPost(httpPost);
    }

    public static String sendHttpPost(String httpUrl, String params, Map<String, String> header) throws Exception {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        // 设置参数
        StringEntity stringEntity = new StringEntity(params, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        return sendHttpPost(httpPost, header);
    }

    public static String sendHttpPostForm(String httpUrl, List<NameValuePair> params, Map<String, String> header) throws Exception {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        // 设置参数
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
        httpPost.setEntity(entity);
        entity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
        return sendHttpPost(httpPost, header);
    }

    public static String sendHttpDelete(String httpUrl) throws Exception {
        // 创建httpDelete
        HttpDelete httpDelete = new HttpDelete(httpUrl);
        return sendHttpDelete(httpDelete);
    }

    public static String sendHttpPut(String httpUrl, String params) throws Exception {
        // 创建httpDelete
        HttpPut httpPut = new HttpPut(httpUrl);
        return sendHttpPut(httpPut);
    }

    public static String sendHttpPut(String httpUrl, String params, Map<String, String> header) throws Exception {
        // 创建httpPost
        HttpPut httpPut = new HttpPut(httpUrl);
        // 设置参数
        StringEntity stringEntity = new StringEntity(params, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPut.setEntity(stringEntity);
        return sendHttpPut(httpPut, header);
    }

    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     * @param params  参数
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> params) throws Exception {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        // 创建参数队列
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
            }
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        return sendHttpPost(httpPost);
    }

    /**
     * 发送Post请求
     *
     * @param httpPost
     * @return
     */
    private static String sendHttpPost(HttpPost httpPost) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                HttpEntity resEntity = response.getEntity();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                EntityUtils.consume(resEntity);
                responseContent = stringBuilder.toString();
                ///responseContent = EntityUtils.toString(resEntity, "UTF-8");
            } else {
                logger.info("请求服务失败，状态码为：" + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    private static String sendHttpPost(HttpPost httpPost, Map<String, String> header) throws Exception {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpPost.setConfig(requestConfig);
            if (header != null && header.size() > 0) {
                for (String key : header.keySet()) {
                    httpPost.setHeader(key, header.get(key));
                }
            }
            // 执行请求
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            // if (statusCode == HttpStatus.SC_OK) {
            HttpEntity resEntity = response.getEntity();
            responseContent = EntityUtils.toString(resEntity, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    private static String sendHttpPut(HttpPut httpPut, Map<String, String> header) throws Exception {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpPut.setConfig(requestConfig);
            if (header != null && header.size() > 0) {
                for (String key : header.keySet()) {
                    httpPut.setHeader(key, header.get(key));
                }
            }
            // 执行请求
            response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            // if (statusCode == HttpStatus.SC_OK) {
            HttpEntity resEntity = response.getEntity();
            responseContent = EntityUtils.toString(resEntity, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    private static String sendHttpDelete(HttpDelete httpDelete) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpDelete.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            // if (statusCode == HttpStatus.SC_OK) {
            HttpEntity resEntity = response.getEntity();
            responseContent = EntityUtils.toString(resEntity, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    private static String sendHttpPut(HttpPut httpPut) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpPut.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            // if (statusCode == HttpStatus.SC_OK) {
            HttpEntity resEntity = response.getEntity();
            responseContent = EntityUtils.toString(resEntity, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    /**
     * 发送 get请求
     *
     * @param httpUrl
     */
    public static String sendHttpGet(String httpUrl) throws Exception {
        HttpGet httpGet = new HttpGet(httpUrl);
        return sendHttpGet(httpGet);
    }

    /**
     * 发送get请求
     *
     * @param httpUrl
     * @param header
     * @return
     * @throws Exception
     */
    public static String sendHttpGet(String httpUrl, Map<String, String> header) throws Exception {
        HttpGet httpGet = new HttpGet(httpUrl);
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpGet.setConfig(requestConfig);
            if (header != null && header.size() > 0) {
                for (String key : header.keySet()) {
                    httpGet.setHeader(key, header.get(key));
                }
            }
            // 执行请求
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                responseContent = EntityUtils.toString(resEntity, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    /**
     * 发送 get请求Https
     *
     * @param httpUrl
     */
    public static String sendHttpsGet(String httpUrl) throws Exception {
        HttpGet httpGet = new HttpGet(httpUrl);
        return sendHttpsGet(httpGet);
    }

    /**
     * 发送Get请求
     *
     * @param httpGet
     * @return
     */
    private static String sendHttpGet(HttpGet httpGet) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                responseContent = EntityUtils.toString(resEntity, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    /**
     * 发送Get请求Https
     *
     * @param httpGet
     * @return
     */
    private static String sendHttpsGet(HttpGet httpGet) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.load(new URL(httpGet.getURI().toString()));
            DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(publicSuffixMatcher);
            httpClient = HttpClients.custom().setSSLHostnameVerifier(hostnameVerifier).build();
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    public static final byte[] readBytes(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime = 0;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    if (readLengthThisTime == -1) {
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return message;
            } catch (IOException e) {
                /// e.printStackTrace();
            }
        }
        return new byte[]{};
    }


    /**
     * POST调用蓝鲸接口获取返回数据
     *
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String httpPostReturn(String url, Map<String, Object> paramMap) throws IOException {
        StringBuffer buffer = new StringBuffer();
        HttpEntity he = null;
        String respContent = null;
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpClient client = PoolManager.getHttpClient();
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry map : paramMap.entrySet()) {
            jsonObject.put(String.valueOf(map.getKey()), map.getValue());
        }
        HttpEntity entity = new StringEntity(jsonObject.toString());
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json, text/plain, */*;charset=utf-8");
        CloseableHttpResponse resp = client.execute(httpPost);
        if (resp.getStatusLine().getStatusCode() == INT200) {
            he = resp.getEntity();
            respContent = EntityUtils.toString(he, "UTF-8");
        }
        if (he != null) {
            EntityUtils.consume(he);
        }
        if (resp != null) {
            resp.close();
        }
        return respContent;
    }

    /**
     * GET调用蓝鲸接口获取返回数据
     *
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String httpGetReturn(String url, Map<String, Object> paramMap) throws IOException {
        HttpEntity he = null;
        String respContent = null;
        CloseableHttpClient client = PoolManager.getHttpClient();
        StringBuffer urlInfo = new StringBuffer();
        urlInfo.append(url);
        urlInfo.append("?");
        urlInfo.append(getUrlParamsByMap(paramMap));
        HttpGet httpGet = new HttpGet(urlInfo.toString());
        httpGet.setHeader("Accept", "application/json, text/plain, */*;charset=utf-8");
        CloseableHttpResponse resp = client.execute(httpGet);
        if (resp.getStatusLine().getStatusCode() == INT200) {
            he = resp.getEntity();
            respContent = EntityUtils.toString(he, "UTF-8");
        } else {
            logger.error("调用返回失败，失败原因：" + resp.toString());
        }
        if (he != null) {
            EntityUtils.consume(he);
        }
        if (resp != null) {
            resp.close();
        }
        return respContent;
    }

    /**
     * 将map转换成url
     *
     * @param map
     * @return
     */
    public static String getUrlParamsByMap(Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String valuestr = getUrlEncoderString(entry.getValue().toString());
            sb.append(entry.getKey() + "=" + valuestr);
            sb.append(SUFFIX);
        }
        String params = sb.toString();
        if (params.endsWith(SUFFIX)) {
            params = StringUtils.substringBeforeLast(params, SUFFIX);
        }
        return params;
    }

    /**
     * URL 转码
     *
     * @param str
     * @return String
     */
    public static String getUrlEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发送 post请求，返回包含statusCode的响应，以便业务逻辑判断
     *
     * @param httpUrl 地址
     * @param params  参数 json
     */
    public static CloseableHttpResponse sendHttpPostReturnResponse(String httpUrl, String params) throws Exception {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        // 设置参数
        StringEntity stringEntity = new StringEntity(params, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     * @param params  参数
     */
    public static String sendHttpPostNotPool(String httpUrl, String params) throws Exception {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        // 设置参数
        StringEntity stringEntity = new StringEntity(params, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        return sendHttpPostNotPool(httpPost);
    }

    /**
     * 发送Post请求
     *
     * @param httpPost
     * @return
     */
    private static String sendHttpPostNotPool(HttpPost httpPost) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String responseContent = null;
        HttpResponse response = null;
        try {
            // 创建默认的httpClient实例.
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                HttpEntity resEntity = response.getEntity();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                EntityUtils.consume(resEntity);
                responseContent = stringBuilder.toString();
                ///responseContent = EntityUtils.toString(resEntity, "UTF-8");
            } else {
                logger.info("请求服务失败，状态码为：" + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                // 关闭连接,释放资源
                if (httpPost != null) {
                    httpPost.releaseConnection();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }
}
