package com.iflytek.ossp.novelcrawler.novelspider.entity;

import com.alibaba.fastjson.JSON;
import com.iflytek.ossp.commonutils.ReadFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Map;

/** 存放抽取规则
 * Created by sypeng on 2016/12/8.
 */
public class PageRule {

    @SuppressWarnings("ConstantConditions")
    private static final String BASEPATH = PageRule.class.getClassLoader().getResource("//").getPath();


    private String uid;

    private String seedUrl;

    private Map<String, List<PointStripRule>> levelStripRules;

    /**
     * 禁止外部创建对象
     */
    private PageRule() {
    }

    /**
     * 创建配置对象, 使用fileName初始化
     * @param fileName 文件名
     * @return ContentStripRule对象
     */
    public static PageRule create(String fileName) {

        String jsonConfigStr = ReadFile.readAll(BASEPATH + fileName, "UTF-8");

        PageRule pageRule = JSON.parseObject(jsonConfigStr, PageRule.class);
        pageRule.setUid(DigestUtils.md5Hex(pageRule.getSeedUrl()));
        return pageRule;
    }

    public String getUid() {
        return uid;
    }

    private void setUid(String uid) {
        this.uid = uid;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public void setSeedUrl(String seedUrl) {
        this.seedUrl = seedUrl;
    }

    public Map<String, List<PointStripRule>> getLevelStripRules() {
        return levelStripRules;
    }

    public void setLevelStripRules(Map<String, List<PointStripRule>> levelStripRules) {
        this.levelStripRules = levelStripRules;
    }
}
