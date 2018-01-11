package com.iflytek.ossp.novelcrawler.novelspider.entity;

/** 抽取点需要进行的操作类型
 * Created by sypeng on 2016/12/10.
 */
public class PointActionType {

    public static final String ADDREQUEST = "01"; //添加爬取请求到队列,包含参数 nextPageType
    public static final String TOPIPELINE = "02"; //保存至pipeline
    public static final String TONEXTPAGE = "03"; //保存至下一页请求页面的爬取 包含参数 requestRef

}
