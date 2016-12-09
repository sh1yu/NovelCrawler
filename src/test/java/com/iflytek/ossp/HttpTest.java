package com.iflytek.ossp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by sypeng on 2016/12/5.
 */
public class HttpTest {
    public static void main(String[] args) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://s1.voicecloud.cn/activity/faq/imeHotWords/index.html");
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(500).setConnectionRequestTimeout(500)
                .setSocketTimeout(1000).build();
        get.setConfig(requestConfig);
        get.setHeader("Accept-Encoding", "/");
        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);
        String content = new String(outputStream.toByteArray(), "UTF-8");
        System.out.println(content);
    }
}
