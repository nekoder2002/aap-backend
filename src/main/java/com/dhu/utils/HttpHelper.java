package com.dhu.utils;

import cn.hutool.http.HttpRequest;
import com.dhu.exception.HttpException;
import org.apache.commons.codec.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class HttpHelper {

    //使用连接池管理连接
    private static final PoolingHttpClientConnectionManager CONNECT_MANAGER;

    private static final RequestConfig.Builder CONFIG_BUILDER;

    @Value("${kb-qa-interface-prefix}")
    private String baseURL;

    static {
        CONNECT_MANAGER = new PoolingHttpClientConnectionManager(60, TimeUnit.SECONDS);
        CONFIG_BUILDER = RequestConfig.custom();

        CONNECT_MANAGER.setMaxTotal(1000);
        CONNECT_MANAGER.setDefaultMaxPerRoute(50);
        CONFIG_BUILDER.setSocketTimeout(3000)//设置客户端等待服务端返回数据的超时时间
                .setConnectTimeout(1000)//设置客户端发起TCP连接请求的超时时间
                .setConnectionRequestTimeout(3000);//设置客户端从连接池获取链接的超时时间
    }

    //创建 httpclient 对象
    private HttpClient getHttpClient() {
        return HttpClients.custom()
                .setConnectionManager(CONNECT_MANAGER)
                .disableAutomaticRetries()
                .build();
    }

    //回收链接到连接池
    private void consume(HttpResponse response) {
        //回收链接到连接池
        if (response != null) {
            try {
                EntityUtils.consume(response.getEntity());
            } catch (IOException e) {
                throw new HttpException(e.getMessage());
            }
        }
    }

    //发送post请求
    public String post(String url, String body) {
        HttpClient httpClient = getHttpClient();
        HttpResponse response = null;
        HttpPost httpPost = new HttpPost(baseURL + url);
        httpPost.setConfig(CONFIG_BUILDER.build());
        httpPost.setHeader("Content-type", MediaType.APPLICATION_JSON_VALUE);
        try {
            httpPost.setEntity(new StringEntity(body));
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, CharEncoding.UTF_8);
            }
        } catch (IOException e) {
            throw new HttpException(e.getMessage());
        } finally {
            consume(response);
        }
        return null;
    }

    //发送GET请求
    public String get(String url, Map<String, String> params) {
        //url参数拼接
        if (params != null && !params.isEmpty()) {
            url = url + '?' +
                    String.join("&", params.entrySet().stream().map(entry -> entry.getKey() + '=' + entry.getValue()).toArray(String[]::new));
        }
        HttpClient httpClient = getHttpClient();
        HttpResponse response = null;
        HttpGet httpGet = new HttpGet(baseURL + url);
        httpGet.setConfig(CONFIG_BUILDER.build());
        try {
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, CharEncoding.UTF_8);
            }
        } catch (IOException e) {
            throw new HttpException(e.getMessage());
        } finally {
            consume(response);
        }
        return null;
    }

    //上传文件
    public String upload(String url, String uuid, MultipartFile file, Map<String, Object> params) {
        File[] upFiles = new File[1];
        upFiles[0] = BaseUtils.toFile(file);
        File reFile=new File(upFiles[0].getParentFile(), uuid + ".pdf");
        upFiles[0].renameTo(reFile);
        upFiles[0]=reFile;
        Map<String, Object> data = new HashMap<>(params);
        data.put("files", upFiles);
        return HttpRequest.post(baseURL + url)
                .form(data)
                .contentType("multipart/form-data")
                .execute()
                .body();
    }
}