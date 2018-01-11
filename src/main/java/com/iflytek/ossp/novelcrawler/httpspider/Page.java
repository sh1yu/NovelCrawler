package com.iflytek.ossp.novelcrawler.httpspider;

import java.util.LinkedList;
import java.util.List;

/** 页面信息
 * Created by sypeng on 2016/11/28.
 */
public class Page {

    private String url;
    private int layer;
    private List<String> links;
    private String parentUrl;

    public Page() {
        this.links = new LinkedList<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }
}
