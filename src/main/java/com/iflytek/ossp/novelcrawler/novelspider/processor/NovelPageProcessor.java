package com.iflytek.ossp.novelcrawler.novelspider.processor;

import com.iflytek.ossp.novelcrawler.novelspider.entity.*;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** 页面处理器
 * Created by sypeng on 2016/12/7.
 */
public class NovelPageProcessor implements PageProcessor {

    private static AtomicLong novelListCount = new AtomicLong(0);
    private static AtomicLong novelCount = new AtomicLong(0);

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

        PageType pageType = page.getResultItems().get("pageType");
        List<PointStripRule> pointStripRules = currentRule.getLevelStripRules().get(pageType.toString());

        processPage(page, pointStripRules);
    }

    @Override
    public Site getSite() {
        return site;
    }


    /**
     * 处理页面
     * @param page page
     * @param pointStripRules List<PointStripRule>
     */
    private void processPage(Page page, List<PointStripRule> pointStripRules) {

        Map<String, Map<String, Object>> nextRequestRefs = new HashMap<>();
        Map<String, PointStripRule> requestLinkRules = new HashMap<>();

        //预处理不同action的point
        for(PointStripRule rule1 : pointStripRules) {

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


                    //书籍数目的限制
                    if(isRequestAddLimited(page.getResultItems())) {
                        break;
                    }

                    Request request = new Request(link);

                    if(nextRequestRefs.get(linkRuleName) != null) {
                        for (String extraName : nextRequestRefs.get(linkRuleName).keySet()) {
                            request.putExtra(extraName, nextRequestRefs.get(linkRuleName).get(extraName));
                        }
                    }

                    request.putExtra("ruleUid", rule.getUid());
                    request.putExtra("pageType", PageType.valueOf(requestLinkRule.getNextPageType()));

                    page.addTargetRequest(request);
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

        List<String> params = pointStripRule.getStripParams();

        switch (pointStripRule.getStripType()) {
            case StripType.HTML_LINKS_REGEX :
                return page.getHtml().links().regex(params.get(0), Integer.parseInt(params.get(1))).all();
            case StripType.HTML_XPATH:
                return page.getHtml().xpath(params.get(0)).toString();
            case StripType.HTML_XPATH_REGEX:
                String str = page.getHtml().xpath(params.get(0)).toString();
                return str.replaceAll(params.get(1), params.get(2));
            case StripType.HTML_XPATH_BLOCK_LISTS:
                List<Selectable> blocks1 = page.getHtml().xpath(params.get(0)).nodes();
                List<List<String>> items1 = new LinkedList<>();
                for(Selectable selectable: blocks1) {
                    items1.add(selectable.xpath(params.get(1)).all());
                }
                return items1;
            case StripType.HTML_XPATH_BLOCK_FLATLIST:
                List<Selectable> blocks2 = page.getHtml().xpath(params.get(0)).nodes();
                List<String> items2 = new LinkedList<>();
                for(Selectable selectable: blocks2) {
                    items2.addAll(selectable.xpath(params.get(1)).all());
                }
                return items2;
            case StripType.URL_REGEX:
                return page.getUrl().toString().replaceAll(params.get(0), params.get(1));
            case StripType.PAGE_FIELD_REF:
                return page.getResultItems().get(params.get(0));
            default:
                return null;
        }
    }

    /**
     * 根据当前页面的resultItems进行判断，是否限制新request的添加
     * @param resultItems resultItems
     * @return true表示限制添加， false表示可以添加
     */
    private boolean isRequestAddLimited(ResultItems resultItems) {
        return resultItems.get("pageType").equals(PageType.SEED) && novelListCount.incrementAndGet() > Config.BOOKLIST_NUMBER_MAXIMUN
                || resultItems.get("pageType").equals(PageType.BOOKLIST) && novelCount.incrementAndGet() > Config.BOOK_NUMBER_MAXIMUN;

    }
}
