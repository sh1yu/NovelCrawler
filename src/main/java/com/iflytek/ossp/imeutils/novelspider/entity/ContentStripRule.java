package com.iflytek.ossp.imeutils.novelspider.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.ossp.commonutils.ReadFile;
import org.apache.commons.codec.digest.DigestUtils;

/** 存放抽取规则
 * Created by sypeng on 2016/12/8.
 */
public class ContentStripRule {

    @SuppressWarnings("ConstantConditions")
    private static final String BASEPATH = ContentStripRule.class.getClassLoader().getResource("//").getPath();


    private String uid;
    private String seedUrl;
    private String bookListUrl;
    private String bookInfoUrl;
    private String chapterListUrl;
    private String chapterDetailUrl;
    private String bookNameXPath;
    private String authorXPath;
    private String imgUrlXPath;
    private String descriptionXPath;
    private String volumeBlockXPath;
    private String chapterNameListXPath;
    private String chapterLinkListXPath;
    private String chapterContentXPath;
    private String chapterTitleXPath;

    /**
     * 禁止外部创建对象
     */
    private ContentStripRule() {
    }

    /**
     * 创建配置对象, 使用fileName初始化
     * @param fileName 文件名
     * @return ContentStripRule对象
     */
    public static ContentStripRule create(String fileName) {

        String jsonConfigStr = ReadFile.readAll(BASEPATH + fileName, "UTF-8");
        JSONObject jsonObject = JSON.parseObject(jsonConfigStr);

        ContentStripRule rule = new ContentStripRule();
        rule.seedUrl = jsonObject.getString("seedUrl");

        rule.uid = DigestUtils.md2Hex(rule.seedUrl);

        JSONObject urlRegexRule = jsonObject.getJSONObject("urlRegexRule");
        rule.bookListUrl = urlRegexRule.getString("bookListUrl");
        rule.bookInfoUrl = urlRegexRule.getString("bookInfoUrl");
        rule.chapterListUrl = urlRegexRule.getString("chapterListUrl");
        rule.chapterDetailUrl = urlRegexRule.getString("chapterDetailUrl");

        JSONObject contentXPathRule = jsonObject.getJSONObject("contentXPathRule");
        rule.bookNameXPath = contentXPathRule.getString("bookName");
        rule.authorXPath = contentXPathRule.getString("author");
        rule.imgUrlXPath = contentXPathRule.getString("imgUrl");
        rule.descriptionXPath = contentXPathRule.getString("description");
        rule.volumeBlockXPath = contentXPathRule.getString("volumeBlock");
        rule.chapterNameListXPath = contentXPathRule.getString("chapterNameList");
        rule.chapterLinkListXPath = contentXPathRule.getString("chapterLinkList");
        rule.chapterContentXPath = contentXPathRule.getString("chapterContent");
        rule.chapterTitleXPath = contentXPathRule.getString("chapterTitle");

        return rule;
    }


    /******* getter **************************/


    public String getUid() {
        return uid;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public String getBookListUrl() {
        return bookListUrl;
    }

    public String getBookInfoUrl() {
        return bookInfoUrl;
    }

    public String getChapterListUrl() {
        return chapterListUrl;
    }

    public String getChapterDetailUrl() {
        return chapterDetailUrl;
    }

    public String getBookNameXPath() {
        return bookNameXPath;
    }

    public String getAuthorXPath() {
        return authorXPath;
    }

    public String getImgUrlXPath() {
        return imgUrlXPath;
    }

    public String getDescriptionXPath() {
        return descriptionXPath;
    }

    public String getVolumeBlockXPath() {
        return volumeBlockXPath;
    }

    public String getChapterNameListXPath() {
        return chapterNameListXPath;
    }

    public String getChapterLinkListXPath() {
        return chapterLinkListXPath;
    }

    public String getChapterContentXPath() {
        return chapterContentXPath;
    }

    public String getChapterTitleXPath() {
        return chapterTitleXPath;
    }
}
