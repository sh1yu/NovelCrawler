package com.iflytek.ossp.imeutils.novelspider.entity;

import java.util.List;

/** 抽取点规则
 * Created by sypeng on 2016/12/9.
 */
public class PointStripRule {

    private String name;
    private String stripType;
    private List<String> params;

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

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
