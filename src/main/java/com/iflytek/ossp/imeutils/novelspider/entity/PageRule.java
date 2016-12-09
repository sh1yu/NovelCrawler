package com.iflytek.ossp.imeutils.novelspider.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.ossp.commonutils.ReadFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

/** 存放抽取规则
 * Created by sypeng on 2016/12/8.
 */
public class PageRule {

    @SuppressWarnings("ConstantConditions")
    private static final String BASEPATH = PageRule.class.getClassLoader().getResource("//").getPath();


    private String uid;

    private String seedUrl;

    private List<PointStripRule> pointStripRules;

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

        return JSON.parseObject(jsonConfigStr, PageRule.class);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public void setSeedUrl(String seedUrl) {
        this.seedUrl = seedUrl;
    }

    public List<PointStripRule> getPointStripRules() {
        return pointStripRules;
    }

    public void setPointStripRules(List<PointStripRule> pointStripRules) {
        this.pointStripRules = pointStripRules;
    }
}
