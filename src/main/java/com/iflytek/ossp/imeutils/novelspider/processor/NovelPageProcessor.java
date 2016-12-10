package com.iflytek.ossp.imeutils.novelspider.processor;

import com.iflytek.ossp.imeutils.novelspider.entity.*;
import com.iflytek.ossp.imeutils.novelspider.utils.StringUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 页面处理器
 * Created by sypeng on 2016/12/7.
 */
public class NovelPageProcessor implements PageProcessor {

    private static AtomicLong novelCount = new AtomicLong(0);
    private static AtomicLong novelListCount = new AtomicLong(0);

    private Site site = Site.me().setRetryTimes(3).setSleepTime(50);

    private PageRule rule ;

    public NovelPageProcessor(PageRule rule) {
        this.rule = rule;
    }

    @Override
    public void process(Page page) {

        // TODO: 2016/12/8 应当根据stripRule的Uid选取所需要的PageRule
        PageRule currentRule = rule;

        //保存上一级传递下来的信息
        for(String key : page.getRequest().getExtras().keySet()) {
            page.putField(key, page.getRequest().getExtra(key));
        }

        processPage(page, currentRule);
    }

    @Override
    public Site getSite() {
        return site;
    }


    /**
     * 处理页面
     * @param page page
     * @param rule rule
     */
    private void processPage(Page page, PageRule rule) {

        Map<String, Map<String, Object>> nextRequestRefs = new HashMap<>();
        Map<String, PointStripRule> requestLinkRules = new HashMap<>();

        //预处理不同action的point
        for(PointStripRule rule1 : rule.getPageTypeStripRules().getBookInfoTypePointRules()) {

            switch (rule1.getPointActionType()) {
                case PointActionType.ADDREQUEST:
                    requestLinkRules.put(rule1.getName(), rule1);
                    break;

                case PointActionType.TONEXTPAGE:
                    Map<String, Object> nextRequestRef = nextRequestRefs.get(rule1.getRequestRef());
                    if(nextRequestRef == null) {
                        nextRequestRef = new HashMap<>();
                        nextRequestRefs.put(rule1.getRequestRef(), nextRequestRef);
                    }
                    nextRequestRef.put(rule1.getName(), processStrip(page, rule1));
                    break;

                case PointActionType.TOPIPELINE:
                    Object element = processStrip(page, rule1);
                    page.putField(rule1.getName(), element);

            }
        }

        //处理添加request的action
        for(String linkRuleName: requestLinkRules.keySet()) {
            PointStripRule requestLinkRule = requestLinkRules.get(linkRuleName);
            //noinspection unchecked
            List<String> listLinks = (List<String>) processStrip(page, requestLinkRule);
            if(listLinks != null) {
                for (String link : listLinks) {
                    Request request = new Request(link);
                    request.putExtra("ruleUid", rule.getUid());
                    request.putExtra("pageType", PageType.valueOf(requestLinkRule.getNextPageType()));
                    for(String extraName : nextRequestRefs.get(linkRuleName).keySet()) {
                        request.putExtra(extraName, nextRequestRefs.get(linkRuleName).get(extraName));
                    }
                }
            }
        }
    }

    /**
     * 根据不同操作类型进行提取
     * @param page page
     * @param pointStripRule 提取点规则
     * @return List&lt;String&gt;或者String, 或者双层List
     */
    private Object processStrip(Page page, PointStripRule pointStripRule) {

        List<String> params = pointStripRule.getstripParams();

        switch (pointStripRule.getStripType()) {
            case StripType.HTML_LINKS_REGEX :
                return page.getHtml().links().regex(params.get(0), Integer.parseInt(params.get(1))).all();
            case StripType.HTML_XPATH:
                return page.getHtml().xpath(params.get(0)).toString();
            case StripType.HTML_XPATH_REGEX:
                String str = page.getHtml().xpath(params.get(0)).toString();
                return str.replaceAll(params.get(1), params.get(2));
            case StripType.HTML_XPATH_BLOCK_LISTS:
                List<Selectable> blocks = page.getHtml().xpath(params.get(0)).nodes();
                List<List<String>> items = new LinkedList<>();
                for(Selectable selectable: blocks) {
                    items.add(selectable.xpath(params.get(1)).all());
                }
                return items;
            case StripType.URL_REGEX:
                return page.getUrl().toString().replaceAll(params.get(0), params.get(1));
            case StripType.PAGE_FIELD_REF:
                return page.getResultItems().get(params.get(0));
            default:
                return null;
        }
    }
}
