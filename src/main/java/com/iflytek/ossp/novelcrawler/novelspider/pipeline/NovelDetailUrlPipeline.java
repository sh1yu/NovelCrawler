package com.iflytek.ossp.novelcrawler.novelspider.pipeline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.ossp.commonutils.ReadFile;
import com.iflytek.ossp.novelcrawler.novelspider.entity.PageType;
import com.iflytek.ossp.novelcrawler.novelspider.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.*;
import java.net.URL;
import java.util.List;

/** 处理所有小说详情页Url的pipeline
 * Created by sypeng on 2016/12/7.
 */
public class NovelDetailUrlPipeline implements Pipeline{

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelDetailUrlPipeline.class);

    private static final String storagePath = "D:\\data\\novelcrawl";

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
        String bookname = resultItems.get("bookName");
        String author = resultItems.get("author");
        String description = resultItems.get("description");
        String imgurl = resultItems.get("imgUrl");

        System.out.println("书名："+bookname + ", 作者："+ author + ", 简介："+ description + ", 封面图片地址："+ imgurl);

        String cleanbookname = StringUtil.filterInvalidFileNameStr(bookname);

        File bookdir = new File(storagePath+"\\"+ cleanbookname);
        if(bookdir.exists()) {
            LOGGER.warn("文件夹"+cleanbookname+"已经存在");
            return;
        }
        if(!bookdir.mkdir()) {
            LOGGER.warn("文件夹"+cleanbookname+"创建不成功");
            return;
        }

        //保存封面
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new URL(imgurl).openStream();
            outputStream = new FileOutputStream(new File(storagePath+"\\"+cleanbookname+"\\封面.jpg"));
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
        } catch (IOException e) {
            LOGGER.error("保存封面失败！");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
            outputStream = new FileOutputStream(new File(storagePath+"\\"+cleanbookname+"\\info.json"));
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
        File chapterDir = new File(storagePath+"\\"+cleanbookname+"\\章节");
        if(!chapterDir.mkdir()) {
            LOGGER.error("创建"+cleanbookname+"的章节文件夹失败！");
        }
    }

    /**
     * 保存章节目录
     * @param resultItems resultItems
     */
    private void processChapterListPage(ResultItems resultItems) {

        List<List<String>> chapternames = resultItems.get("chapterNameList");
        String bookname = resultItems.get("bookName");
        String cleanbookname = StringUtil.filterInvalidFileNameStr(bookname);
        File chapterjson = new File(storagePath+"\\"+ cleanbookname +"\\章节.json");
        JSONArray jsonArray = new JSONArray();

        if(chapterjson.exists()) {
            String jsonstr = ReadFile.readAll(storagePath+"\\"+cleanbookname+"\\章节.json", "utf-8");
            jsonArray = JSON.parseArray(jsonstr);
        }

        for(int i=0; i<chapternames.size(); i++) {
            List<String> ss = chapternames.get(i);
            String indexPrefix = i+"-";
            for(int j=0; j<ss.size(); j++) {
                String cleanChapterName = StringUtil.filterInvalidFileNameStr(ss.get(j));
                JSONObject object = new JSONObject();
                object.put("title", cleanChapterName);
                object.put("index", indexPrefix+j);
                jsonArray.add(object);
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

        String bookName = resultItems.get("bookName");
        String chapterTitle = resultItems.get("chapterTitle");
        String chapterContent = resultItems.get("chapterContent");


        //排除没有内容的章节
        if(StringUtils.isBlank(bookName) || StringUtils.isBlank(chapterTitle) || StringUtils.isBlank(chapterContent)) {
            return;
        }

        String cleanBookName = StringUtil.filterInvalidFileNameStr(bookName);

        File chapterDir = new File(storagePath+"\\"+cleanBookName+"\\章节");
        if(!chapterDir.exists()) {
            return;
        }

        File chapter = new File(storagePath+"\\"+cleanBookName+"\\章节\\"+chapterTitle + ".txt");
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(chapter));
            writer.println(chapterContent);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
