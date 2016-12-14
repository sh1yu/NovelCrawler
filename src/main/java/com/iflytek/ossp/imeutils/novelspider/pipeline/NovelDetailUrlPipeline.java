package com.iflytek.ossp.imeutils.novelspider.pipeline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.ossp.commonutils.ReadFile;
import com.iflytek.ossp.imeutils.novelspider.entity.Config;
import com.iflytek.ossp.imeutils.novelspider.entity.PageType;
import com.iflytek.ossp.imeutils.novelspider.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.*;
import java.util.List;

/** 处理所有小说详情页Url的pipeline
 * Created by sypeng on 2016/12/7.
 */
public class NovelDetailUrlPipeline implements Pipeline{

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelDetailUrlPipeline.class);

    private static final String storagePath = Config.STORAGE_DIR;

    @Override
    public void process(ResultItems resultItems, Task task) {

        PageType pageType = resultItems.get("pageType");
        switch (pageType) {
            case BOOKINFO:
                processBookInfoPage(resultItems);
                break;
            case CHAPTERLIST:
                processChapterListPage(resultItems);
                break;
            case CHAPTERCONTENT:
                processChapterContentPage(resultItems);
                break;
        }
    }

    /**
     * 保存书籍详情页
     * @param resultItems resultItems
     */
    private void processBookInfoPage(ResultItems resultItems) {

        //不处理页面
        Boolean discard = resultItems.get("discard");
        if(discard != null && discard.equals(Boolean.TRUE)) {
            return;
        }

        String bookStyle = resultItems.get("bookStyle");

        String bookname = resultItems.get("bookName");
        String author = resultItems.get("author");
        String description = resultItems.get("description");
        String imgurl = resultItems.get("imgUrl");

        LOGGER.debug("书名："+bookname + ", 作者："+ author + ", 简介："+ description + ", 封面图片地址："+ imgurl);

        String cleanbookname = StringUtil.filterInvalidFileNameStr(bookname);

        String fileDirStr;
        if(Config.BOOK_STYLE_ENABLE && StringUtils.isNotEmpty(bookStyle)) {
            File styleDir = new File(storagePath + File.separator + StringUtil.filterInvalidFileNameStr(bookStyle));
            if(! styleDir.exists() && ! styleDir.mkdir()) {
                LOGGER.warn("文件夹"+StringUtil.filterInvalidFileNameStr(bookStyle)+"创建不成功");
            }
            fileDirStr = storagePath+File.separator+StringUtil.filterInvalidFileNameStr(bookStyle)+File.separator+cleanbookname;
        } else {
            fileDirStr = storagePath+File.separator + cleanbookname;
        }
        File bookdir = new File(fileDirStr);
        if(bookdir.exists()) {
            LOGGER.warn("文件夹"+cleanbookname+"已经存在");
            return;
        }
        if(!bookdir.mkdir()) {
            LOGGER.warn("文件夹"+cleanbookname+"创建不成功");
            return;
        }

        //保存封面
        OutputStream outputStream = null;
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(imgurl);
            RequestConfig.Builder configBuilder = RequestConfig.custom().setConnectTimeout(200).setSocketTimeout(1000);
            httpGet.setConfig(configBuilder.build());
            HttpResponse response = httpClient.execute(httpGet);
            outputStream = new FileOutputStream(new File(fileDirStr + File.separator +"封面.jpg"));
            response.getEntity().writeTo(outputStream);
        } catch (IOException e) {
            LOGGER.error("保存封面失败！");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //info.json
        StringBuilder stringBuilder = new StringBuilder("{\n");
        stringBuilder.append("\t\"author\" : \"").append(author).append("\",\n");
        stringBuilder.append("\t\"bookName\" : \"").append(bookname).append("\",\n");
        stringBuilder.append("\t\"description\" : \"").append(description).append("\"\n");
        stringBuilder.append("}");


        try {
            outputStream = new FileOutputStream(new File(fileDirStr + File.separator +"info.json"));
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println(stringBuilder.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("info.json创建失败！");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //章节文件夹
        File chapterDir = new File(fileDirStr + File.separator + "章节");
        if(!chapterDir.mkdir()) {
            LOGGER.error("创建"+cleanbookname+"的章节文件夹失败！");
        }
    }

    /**
     * 保存章节目录
     * @param resultItems resultItems
     */
    private void processChapterListPage(ResultItems resultItems) {

        //不处理页面
        Boolean discard = resultItems.get("discard");
        if(discard != null && discard.equals(Boolean.TRUE)) {
            return;
        }

        List<List<String>> chapternames = resultItems.get("chapterNameList");
        String bookStyle = resultItems.get("bookStyle");
        String bookname = resultItems.get("bookName");
        String cleanbookname = StringUtil.filterInvalidFileNameStr(bookname);

        String fileDirStr;
        if(Config.BOOK_STYLE_ENABLE && StringUtils.isNotEmpty(bookStyle)) {
            fileDirStr = storagePath+File.separator+StringUtil.filterInvalidFileNameStr(bookStyle)+File.separator+cleanbookname;
        } else {
            fileDirStr = storagePath+ File.separator + cleanbookname;
        }

        File chapterjson = new File(fileDirStr+ File.separator +"章节.json");
        JSONArray jsonArray = new JSONArray();

        if(chapterjson.exists()) {
            String jsonstr = ReadFile.readAll(fileDirStr + File.separator + "章节.json", "utf-8");
            jsonArray = JSON.parseArray(jsonstr);
        }


        if(chapternames == null || chapternames.size()==0) {
            return;
        } else if(Config.CHAPTERLIST_ISUSEVOLUME){ //分卷小说的章节
            for (int i = 0; i < chapternames.size(); i++) {
                List<String> ss = chapternames.get(i);
                if(ss == null || ss.size()==0) {
                    return;
                }
                String indexPrefix = i + "-";
                for (int j = 0; j < ss.size(); j++) {
                    String cleanChapterName = StringUtil.filterInvalidFileNameStr(ss.get(j));
                    JSONObject object = new JSONObject();
                    object.put("title", cleanChapterName);
                    object.put("index", indexPrefix + j);
                    jsonArray.add(object);
                }
            }
        } else { //不分卷小说
            int chapterCount = 0;
            for (List<String> ss : chapternames) {
                if (ss == null || ss.size() == 0) {
                    return;
                }
                for (String s : ss) {
                    String cleanChapterName = StringUtil.filterInvalidFileNameStr(s);
                    JSONObject object = new JSONObject();
                    object.put("title", cleanChapterName);
                    object.put("index", chapterCount);
                    jsonArray.add(object);
                    chapterCount++;
                }
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(chapterjson));
            writer.println(JSON.toJSONString(jsonArray, true));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存章节
     * @param resultItems resultItems
     */
    private void processChapterContentPage(ResultItems resultItems) {

        //不处理页面
        Boolean discard = resultItems.get("discard");
        if(discard != null && discard.equals(Boolean.TRUE)) {
            return;
        }

        String bookStyle = resultItems.get("bookStyle");
        String bookName = resultItems.get("bookName");
        String chapterTitle = resultItems.get("chapterTitle");
        String chapterContent = resultItems.get("chapterContent");

        String cleanBookName = StringUtil.filterInvalidFileNameStr(bookName);
        String cleanChapterTitle = StringUtil.filterInvalidFileNameStr(chapterTitle);

        //排除没有内容的章节
        if(StringUtils.isBlank(bookName) || StringUtils.isBlank(chapterTitle) || StringUtils.isBlank(chapterContent)) {
            LOGGER.warn("章节页未爬取到内容！bookName:" + bookName );
            return;
        }

        String fileDirStr;
        if(Config.BOOK_STYLE_ENABLE && StringUtils.isNotEmpty(bookStyle)) {
            fileDirStr = storagePath+File.separator+StringUtil.filterInvalidFileNameStr(bookStyle)+File.separator+cleanBookName;
        } else {
            fileDirStr = storagePath+ File.separator + cleanBookName;
        }


        File chapterDir = new File(fileDirStr + File.separator +"章节");
        if(!chapterDir.exists()) {
            return;
        }

        File chapter = new File(fileDirStr + File.separator +"章节"+ File.separator + cleanChapterTitle + ".txt");
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(chapter));
            writer.println(chapterContent);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
