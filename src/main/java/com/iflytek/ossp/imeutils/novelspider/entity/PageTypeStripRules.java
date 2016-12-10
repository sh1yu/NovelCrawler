package com.iflytek.ossp.imeutils.novelspider.entity;

import java.util.List;

/** 每个页面层级（页面类型）的抽取规则
 * Created by sypeng on 2016/12/10.
 */
public class PageTypeStripRules {

    private List<PointStripRule> seedTypePointRules;
    private List<PointStripRule> bookListTypePointRules;
    private List<PointStripRule> bookInfoTypePointRules;
    private List<PointStripRule> chapterListTypePointRules;
    private List<PointStripRule> chapterContentTypePointRules;

    public List<PointStripRule> getSeedTypePointRules() {
        return seedTypePointRules;
    }

    public void setSeedTypePointRules(List<PointStripRule> seedTypePointRules) {
        this.seedTypePointRules = seedTypePointRules;
    }

    public List<PointStripRule> getBookListTypePointRules() {
        return bookListTypePointRules;
    }

    public void setBookListTypePointRules(List<PointStripRule> bookListTypePointRules) {
        this.bookListTypePointRules = bookListTypePointRules;
    }

    public List<PointStripRule> getBookInfoTypePointRules() {
        return bookInfoTypePointRules;
    }

    public void setBookInfoTypePointRules(List<PointStripRule> bookInfoTypePointRules) {
        this.bookInfoTypePointRules = bookInfoTypePointRules;
    }

    public List<PointStripRule> getChapterListTypePointRules() {
        return chapterListTypePointRules;
    }

    public void setChapterListTypePointRules(List<PointStripRule> chapterListTypePointRules) {
        this.chapterListTypePointRules = chapterListTypePointRules;
    }

    public List<PointStripRule> getChapterContentTypePointRules() {
        return chapterContentTypePointRules;
    }

    public void setChapterContentTypePointRules(List<PointStripRule> chapterContentTypePointRules) {
        this.chapterContentTypePointRules = chapterContentTypePointRules;
    }
}
