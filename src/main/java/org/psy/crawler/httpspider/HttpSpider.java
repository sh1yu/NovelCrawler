package org.psy.crawler.httpspider;

import org.psy.crawler.commonutils.ReadFile;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 爬取给定的一批网页中所有的http链接地址
 * 如果是网页（htm,html,jsp），则进行递归爬取；第一次爬取深度为1，限制最大深度
 * 如果是资源文件(js)，则只进行一次爬取
 * Created by sypeng on 2016/11/28.
 */
public class HttpSpider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSpider.class);

    private static String urlReg = "[\"'][ ]*(http://[^\"']+)[\"']";
    private static Pattern pattern = Pattern.compile(urlReg);
    private static String urlReg2 = "src=[\"'] *([^\"' ]+)[\"']";
    private static Pattern pattern2 = Pattern.compile(urlReg2);

    public static void main(String[] args) {

        @SuppressWarnings("ConstantConditions")
        String basePath = HttpSpider.class.getClassLoader().getResource("//").getPath();
        List<String> waitForCrawl = ReadFile.getAllLine2Array(basePath + "urls.txt");
        Queue<Page> queue = new ConcurrentLinkedDeque<>();
        for(String url: waitForCrawl) {
            Page page = new Page();
            page.setUrl(url.trim());
            page.setLayer(1);
            page.setParentUrl("");
            queue.add(page);
        }
        Set<String> finishedUrl = new ConcurrentSkipListSet<>();
        Queue<Page> found = new ConcurrentLinkedDeque<>();

        AtomicInteger count = new AtomicInteger(0);

        while( ! queue.isEmpty()){
            Page page = queue.poll();
            found.add(page);
            count.incrementAndGet();
            try {
                //获取内容
                HttpClient client = HttpClients.createDefault();
                String url = page.getUrl();
                String[] token = url.split("\\?", 2);
                if (token.length == 2 && token[1].contains("=#")) {
                    url = token[0] + "?" + token[1].replace("#", URLEncoder.encode("#", "UTF-8"));
                }

                //如果url重复，跳过
                if(finishedUrl.contains(url)) {
                    continue;
                } else {
                    finishedUrl.add(url);
                }

                //如果超过级别，跳过
                if(page.getUrl().contains(".js?") || page.getUrl().endsWith("js")) {
                    if(page.getLayer() > 3) {
                        continue;
                    }
                } else {
                    if(page.getLayer() > 2) {
                        continue;
                    }
                }


                LOGGER.info("开始爬取url:"+url);
                HttpGet get = new HttpGet(url);
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
                processPage(queue, found, page, content);
            } catch ( UnknownHostException | ConnectTimeoutException | SocketTimeoutException e) {
                LOGGER.error("连接失败 "+page.getUrl());
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("完成，共"+found.size()+"条");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream("out.txt"));
            Set<String> cacheurl1 = new HashSet<>();
            Set<String> cacheurl2 = new HashSet<>();
            for(Page page: found) {
                if(page.getLayer()==1) {
                    writer.println(page.getUrl());
                    cacheurl1.clear();
                    for(Page page2: found) {
                        if(page2.getLayer()==2 && page2.getParentUrl().equals(page.getUrl())) {
                            if (cacheurl1.contains(page2.getUrl())) {
                                continue;
                            } else {
                                cacheurl1.add(page2.getUrl());
                                writer.println("\t|- "+page2.getUrl());
                            }
                            cacheurl2.clear();
                            for(Page page3 : found) {
                                if(page3.getLayer()==3 && page3.getParentUrl().equals(page2.getUrl())) {
                                    if ( ! cacheurl2.contains(page2.getUrl())) {
                                        cacheurl2.add(page2.getUrl());
                                        writer.println("\t\t|-"+page3.getUrl());
                                    }
                                }
                            }
                            writer.println();
                        }
                    }
                    writer.println();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    /**
     * 处理每一个网页
     * @param queue Queue
     * @param found 完成的列表
     * @param page Page
     * @param content 内容
     */
    private static void processPage(Queue<Page> queue, Queue<Page> found, Page page, String content) {
        if (StringUtils.isBlank(content)) {
            LOGGER.warn("内容为空, url:"+page.getUrl());
        }
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()) {
            String link = matcher.group(1).trim();
            Page childPage = new Page();
            childPage.setParentUrl(page.getUrl());
            childPage.setLayer(page.getLayer()+1);
            childPage.setUrl(link);
            found.add(childPage);
            page.getLinks().add(link);
            if(isHtmlPage(link)) {
                queue.add(childPage);
            }
        }

        Matcher matcher2 = pattern2.matcher(content);
        while(matcher2.find()) {
            String link = matcher2.group(1).trim();
            Page childPage = new Page();
            childPage.setParentUrl(page.getUrl());
            childPage.setLayer(page.getLayer()+1);
            childPage.setUrl(link);
            found.add(childPage);
            page.getLinks().add(link);
            if(isHtmlPage(link)) {
                queue.add(childPage);
            }
        }
    }

    /**
     * 排除不是网页的链接
     * @param url url
     * @return 是否为网页
     */
    private static boolean isHtmlPage(String url) {
        return !url.endsWith("jpg")
                && !url.endsWith("png")
                && !url.endsWith("apk") && !url.contains(".apk?")
                && !url.endsWith("ico")
                && !url.endsWith("dtd")
                && !url.endsWith("exe");
    }
}
