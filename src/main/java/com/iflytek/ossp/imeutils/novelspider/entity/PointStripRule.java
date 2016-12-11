package com.iflytek.ossp.imeutils.novelspider.entity;

import java.util.List;

/** 抽取点规则
 * Created by sypeng on 2016/12/9.
 */
public class PointStripRule {

    private String name;
    private String stripType;
    private List<String> stripParams;
    private String pointActionType;
    private String nextPageType;
    private String requestRef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStripType() {
        return stripType;
    }

    public void setStripType(String stripType) {
        this.stripType = stripType;
    }

    public List<String> getStripParams() {
        return stripParams;
    }

    public void setStripParams(List<String> stripParams) {
        this.stripParams = stripParams;
    }

    public String getPointActionType() {
        return pointActionType;
    }

    public void setPointActionType(String pointActionType) {
        this.pointActionType = pointActionType;
    }

    public String getNextPageType() {
        return nextPageType;
    }

    public void setNextPageType(String nextPageType) {
        this.nextPageType = nextPageType;
    }

    public String getRequestRef() {
        return requestRef;
    }

    public void setRequestRef(String requestRef) {
        this.requestRef = requestRef;
    }
}
