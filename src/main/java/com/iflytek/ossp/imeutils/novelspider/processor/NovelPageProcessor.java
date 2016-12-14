package com.iflytek.ossp.imeutils.novelspider.processor;

import com.iflytek.ossp.imeutils.novelspider.entity.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 页面处理器
 * Created by sypeng on 2016/12/7.
 */
public class NovelPageProcessor implements PageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelPageProcessor.class);

    private static AtomicLong novelListCount = new AtomicLong(0);
    private static AtomicLong novelCount = new AtomicLong(0);

    private static Map<String, AtomicLong> extraCounts = new HashMap<>();

    static {
        if(Config.BOOK_STYLE_ENABLE) {
            for(String key : Config.BOOK_STYLE_NUMBER.keySet()) {
                extraCounts.put(key, new AtomicLong(0));
            }
        }
    }



    private Site site = Site.me().setRetryTimes(Config.PAGE_DOWNLOAD_RETRYTIMES)
            .setSleepTime(Config.PAGE_DOWNLOAD_SLEEPTIME).setTimeOut(Config.PAGE_DOWNLOAD_TIMEOUT);

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
        Map<String, Map<String, Object>> linkAddRequests = new HashMap<>();
        Map<String, Object> preparedPageFields = new HashMap<>();

        //预处理不同action的point
        for(PointStripRule rule1 : pointStripRules) {

            switch (rule1.getPointActionType()) {
                case PointActionType.ADDREQUEST:
                    Map<String, Object> linkAddRequest = new HashMap<>();
                    linkAddRequest.put("links", processStrip(page, rule1));
                    linkAddRequest.put("nextPageType", PageType.valueOf(rule1.getNextPageType()));
                    linkAddRequests.put(rule1.getName(), linkAddRequest);
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
                    preparedPageFields.put(rule1.getName(), processStrip(page, rule1));
            }
        }

        //过滤前处理
        beforeFilterProcessed(page.getResultItems(), nextRequestRefs, linkAddRequests, preparedPageFields);

        //当前页面处理并过滤
        if(pageFilter(page.getResultItems(), nextRequestRefs, linkAddRequests, preparedPageFields)) {
            page.putField("discard", true);
            return;
        }

        //过滤后处理
        afterFilterProcessed(page.getResultItems(), nextRequestRefs, linkAddRequests, preparedPageFields);


        //处理pipeline 的 field
        for(String s: preparedPageFields.keySet()) {
            page.putField(s, preparedPageFields.get(s));
        }

        //处理添加request的action
        for(String linkAddRequestName: linkAddRequests.keySet()) {
            Map<String, Object> linkAddRequest = linkAddRequests.get(linkAddRequestName);
            //noinspection unchecked
            List<String> listLinks = (List<String>) linkAddRequest.get("links");
            PageType nextPageType = (PageType) linkAddRequest.get("nextPageType");


            if(listLinks != null) {
                for (String link : listLinks) {

                    Request request = new Request(link);

                    if(nextRequestRefs.get(linkAddRequestName) != null) {
                        for (String extraName : nextRequestRefs.get(linkAddRequestName).keySet()) {
                            request.putExtra(extraName, nextRequestRefs.get(linkAddRequestName).get(extraName));
                        }
                    }

                    request.putExtra("ruleUid", rule.getUid());
                    request.putExtra("pageType", nextPageType);

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
                if(StringUtils.isEmpty(str)) {
                    return "";
                }
                Pattern pattern = Pattern.compile(params.get(1));
                Matcher matcher = pattern.matcher(str);
                if(matcher.matches()) {
                    return matcher.group(Integer.parseInt(params.get(2)));
                } else {
                    return "";
                }
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
            case StripType.HTML_XPATH_LIST_FLATEN:
                List<String> list = page.getHtml().xpath(params.get(0)).all();
                StringBuilder builder = new StringBuilder();
                if(list == null) {
                    return "";
                }
                for(String str2 : list) {
                    builder.append(str2);
                }
                return builder.toString();
            case StripType.HTML_XPATH_BLOCK_FLATLINKS_REGEX:
                return page.getHtml().xpath(params.get(0)).links().regex(params.get(1), Integer.parseInt(params.get(2))).all();

            case StripType.URL_REGEX:
                return page.getUrl().toString().replaceAll(params.get(0), params.get(1));
            case StripType.PAGE_FIELD_REF:
                return page.getResultItems().get(params.get(0));
            case StripType.RUNJS_NOPARAM_STRLIST:
                try {
                    ScriptEngineManager manager = new ScriptEngineManager();
                    ScriptEngine engine = manager.getEngineByName("JavaScript");
                    engine.eval(params.get(0));

                    //noinspection unchecked
                    List<String> originresult = (List<String>) ((Invocable)engine).invokeFunction(params.get(1));
                    List<String> result = new LinkedList<>();
                    result.addAll(originresult);
                    return result;

                } catch (ScriptException | NoSuchMethodException e) {
                    return new LinkedList<>();
                }
            default:
                return null;
        }
    }

    /**
     * 对页面数据进行过滤
     * @param resultItems ResultItems
     * @param nextRequestRefs 添加新爬取链接是附加数据
     * @param linkAddRequests  新爬取链接规则
     * @param preparedPageFields pipeline处理数据
     * @return true表示过滤掉
     */
    private boolean pageFilter(ResultItems resultItems,
                              Map<String, Map<String, Object>> nextRequestRefs,
                               Map<String, Map<String, Object>> linkAddRequests,
                              Map<String, Object> preparedPageFields) {

        /**分类页面过滤**/
        if(Config.BOOK_STYLE_ENABLE && resultItems.get("pageType").equals(PageType.BOOKINFO)) {

            String bookStyle = (String)preparedPageFields.get("bookStyle");
            AtomicLong count = extraCounts.get(bookStyle);

            if(count == null) {
                LOGGER.warn("未配置类型："+bookStyle);
                return true;
            }

            //当前类型配置了，数目超过了配置值时过滤掉
            return Config.BOOK_STYLE_NUMBER.get(bookStyle) != 0
                    && count.incrementAndGet() > Config.BOOK_STYLE_NUMBER.get(bookStyle);
        }


        /**添加新链接数目限制**/
        for(String key: linkAddRequests.keySet()) {

            //noinspection unchecked
            List<String> links = (List<String>)linkAddRequests.get(key).get("links");
            PageType nextPageType = (PageType)linkAddRequests.get(key).get("nextPageType");

            Set<String> linksSet = new HashSet<>();
            linksSet.addAll(links);
            links.clear();
            for (String link : linksSet) {
                if(PageType.BOOKLIST.equals(nextPageType)
                && Config.BOOKLIST_NUMBER_MAXIMUN != 0
                && novelListCount.incrementAndGet() > Config.BOOKLIST_NUMBER_MAXIMUN
                || PageType.BOOKINFO.equals(nextPageType)
                && Config.BOOK_NUMBER_MAXIMUN != 0
                && novelCount.incrementAndGet() > Config.BOOK_NUMBER_MAXIMUN) {
                    continue;
                }
                links.add(link);
            }
        }

        /**没有类型判断或者其他页面，直接不过滤**/
        return false;
    }

    /**
     * 过滤前处理逻辑
     * @param resultItems ResultItems
     * @param nextRequestRefs 添加新爬取链接是附加数据
     * @param linkAddRequests  新爬取链接规则
     * @param preparedPageFields pipeline处理数据
     */
    private void beforeFilterProcessed(ResultItems resultItems,
                                       Map<String, Map<String, Object>> nextRequestRefs,
                                       Map<String, Map<String, Object>> linkAddRequests,
                                       Map<String, Object> preparedPageFields) {
        //处理小说分类
        if(Config.BOOK_STYLE_ENABLE && resultItems.get("pageType").equals(PageType.BOOKINFO)) {


            outer: for (String key : nextRequestRefs.keySet()) {
                String bookStyle = (String) nextRequestRefs.get(key).get("bookStyle");
                for (String s : Config.BOOK_STYLE_NUMBER.keySet()) {
                    if (StringUtils.isNotEmpty(bookStyle) && bookStyle.contains(s)) {
                        nextRequestRefs.get(key).put("bookStyle", s);
                        break outer;
                    }
                }
            }


            String bookStyle = (String) preparedPageFields.get("bookStyle");
            for (String s : Config.BOOK_STYLE_NUMBER.keySet()) {
                if (StringUtils.isNotEmpty(bookStyle) && bookStyle.contains(s)) {
                    preparedPageFields.put("bookStyle", s);
                    break;
                }
            }


        }
    }


    /**
     * 过滤后处理逻辑
     * @param resultItems ResultItems
     * @param nextRequestRefs 添加新爬取链接是附加数据
     * @param linkAddRequests  新爬取链接规则
     * @param preparedPageFields pipeline处理数据
     */
    private void afterFilterProcessed(ResultItems resultItems,
                                       Map<String, Map<String, Object>> nextRequestRefs,
                                      Map<String, Map<String, Object>> linkAddRequests,
                                       Map<String, Object> preparedPageFields) {

        //do nothing
    }
}
