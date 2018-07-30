package org.psy.crawler;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** httpGet下载文件
 * Created by sypeng on 2016/12/14.
 */
public class HttpGetFileTest {

    public static void main(String[] args) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://static.zongheng.com/upload/cover/2013/11/1384390302518.jpg");
        RequestConfig.Builder configBuilder = RequestConfig.custom().setConnectTimeout(200).setSocketTimeout(1000);
        httpGet.setConfig(configBuilder.build());
        HttpResponse response = httpClient.execute(httpGet);
        OutputStream outputStream = new FileOutputStream(new File("test.jpg"));
        response.getEntity().writeTo(outputStream);
        outputStream.close();
    }
}
