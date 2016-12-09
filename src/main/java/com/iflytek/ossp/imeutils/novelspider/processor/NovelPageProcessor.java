package com.iflytek.ossp.imeutils.novelspider.processor;

import com.iflytek.ossp.imeutils.novelspider.entity.*;
import com.iflytek.ossp.imeutils.novelspider.utils.StringUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

    private static ConcurrentHashMap<String, String> id2BookNameMap = new ConcurrentHashMap<>();

    private Site site = Site.me().setRetryTimes(3).setSleepTime(50);

    private PageRule rule ;

    public NovelPageProcessor(PageRule rule) {
        this.rule = rule;
    }

    @Override
    public void process(Page page) {

        // TODO: 2016/12/8 应当根据stripRule的Uid选取所需要的PageRule
        PageRule currentRule = rule;

        PageType pageType = (PageType) page.getRequest().getExtra("pageType");
        page.putField("pageType", pageType);

        switch (pageType) {
            case SEED:
                processSeedPage(page, currentRule);
                break;
            case BOOKLIST:
                processBookListPage(page, currentRule);
                break;
            case BOOKINFO:
                processBookInfoPage(page, currentRule);
                break;
            case CHAPTERLIST:
                processChapterListPage(page, currentRule);
                break;
            case CHAPTERCONTENT:
                processChapterContentPage(page, currentRule);
                break;
        }
    }



    @Override
    public Site getSite() {
        return site;
    }


    /**
     * 处理种子页
     * @param page page
     * @param rule rule
     */
    private void processSeedPage(Page page, PageRule rule) {

        PointStripRule pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("bookListLinks")) {
                pointStripRule = rule1;
                break;
            }
        }

        if(pointStripRule != null) {

            //noinspection unchecked
            List<String> bookListLinks = (List<String>) processStrip(page, pointStripRule);

            //将书籍列表页加入待爬队列
            if(bookListLinks != null) {
                for (String bookListUrl : bookListLinks) {
                    if (novelListCount.getAndIncrement() < Config.BOOKLIST_NUMBER_MAXIMUN) {
                        Request request = new Request(bookListUrl);
                        request.putExtra("ruleUid", rule.getUid());
                        request.putExtra("pageType", PageType.BOOKLIST);
                        page.addTargetRequest(request);
                    }
                }
            }
        }

    }

    /**
     * 处理书籍列表页
     * @param page page
     * @param rule rule
     */
    private void processBookListPage(Page page, PageRule rule) {

        PointStripRule pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("bookInfoLinks")) {
                pointStripRule = rule1;
                break;
            }
        }

        if(pointStripRule != null) {

            //noinspection unchecked
            List<String> bookInfoLinks = (List<String>) processStrip(page, pointStripRule);

            //将书籍信息页加入待爬队列
            if(bookInfoLinks != null) {
                for(String bookInfoUrl : bookInfoLinks) {
                    if(novelCount.getAndIncrement() < Config.BOOK_NUMBER_MAXIMUN) {
                        Request request = new Request(bookInfoUrl);
                        request.putExtra("stripRuleUid", rule.getUid());
                        request.putExtra("pageType", PageType.BOOKINFO);
                        page.addTargetRequest(request);
                    }
                }
            }
        }
    }

    /**
     * 处理书籍详情页
     * @param page page
     * @param rule rule
     */
    private void processBookInfoPage(Page page, PageRule rule) {

        PointStripRule pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("novelId")) {
                pointStripRule = rule1;
                break;
            }
        }
        if(pointStripRule == null) {
           return;
        }
        String novelId = (String) processStrip(page, pointStripRule);

        pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("bookName")) {
                pointStripRule = rule1;
                break;
            }
        }
        if(pointStripRule == null) {
            return;
        }
        String bookName = (String) processStrip(page, pointStripRule);

        pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("author")) {
                pointStripRule = rule1;
                break;
            }
        }
        if(pointStripRule == null) {
            return;
        }
        String author = (String) processStrip(page, pointStripRule);

        pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("imgUrl")) {
                pointStripRule = rule1;
                break;
            }
        }
        if(pointStripRule == null) {
            return;
        }
        String imgUrl = (String) processStrip(page, pointStripRule);

        pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("description")) {
                pointStripRule = rule1;
                break;
            }
        }
        if(pointStripRule == null) {
            return;
        }
        String description = (String) processStrip(page, pointStripRule);

        pointStripRule = null;
        for(PointStripRule rule1 : rule.getPointStripRules()) {
            if (rule1.getName().equals("chapterListLinks")) {
                pointStripRule = rule1;
                break;
            }
        }
        if(pointStripRule == null) {
            return;
        }
        //noinspection unchecked
        List<String> chapterListLinks = (List<String>) processStrip(page, pointStripRule);
        if (chapterListLinks == null) {
            return;
        }

        for(String chapterlisturl : chapterListLinks) {
            Request request = new Request(chapterlisturl);
            request.putExtra("stripRuleUid", rule.getUid());
            request.putExtra("pageType", PageType.CHAPTERLIST);
            page.addTargetRequest(request);
        }

            Pattern pattern = Pattern.compile(rule.getBookInfoUrl());
        Matcher matcher = pattern.matcher(page.getUrl().toString());
        if(matcher.matches()) {
            String novelid = matcher.group(1);
            page.putField("novelid", novelid);
            String bookname = page.getHtml().xpath(rule.getBookNameXPath()).toString();
            page.putField("bookname", bookname);

            id2BookNameMap.put(rule.getUid()+"_"+novelid, StringUtil.filterInvalidFileNameStr(bookname));

            page.putField("author", page.getHtml().xpath(rule.getAuthorXPath()).toString());
            page.putField("description", page.getHtml().xpath(rule.getDescriptionXPath()).toString());
            page.putField("imgurl", page.getHtml().xpath(rule.getImgUrlXPath()).toString());

            List<String> chapterListIds = page.getHtml().links().regex(rule.getChapterListUrl(), 0).all();

        }
    }

    /**
     * 处理章节列表页
     * @param page page
     * @param rule rule
     */
    private void processChapterListPage(Page page, PageRule rule) {

        Pattern pattern = Pattern.compile(rule.getChapterListUrl());
        Matcher matcher = pattern.matcher(page.getUrl().toString());
        if(matcher.matches()) {
            String novelid = matcher.group(1);
            page.putField("cleanbookname", id2BookNameMap.get(rule.getUid()+"_"+novelid));

            List<Selectable> volumeBlocks = page.getHtml().xpath(rule.getVolumeBlockXPath()).nodes();

            List<List<String>> chapterNames = new LinkedList<>();

            for (Selectable selectable : volumeBlocks) {
                chapterNames.add(selectable.xpath(rule.getChapterNameListXPath()).all());

                for(String chapterLink : selectable.xpath(rule.getChapterLinkListXPath()).all()) {
                    Request request = new Request(chapterLink);
                    request.putExtra("stripRuleUid", rule.getUid());
                    request.putExtra("pageType", PageType.CHAPTERCONTENT);
                    page.addTargetRequest(request);
                }
            }

            page.putField("chapternames", chapterNames);

        }
    }

    /**
     * 处理章节详情页
     * @param page page
     * @param rule rule
     */
    private void processChapterContentPage(Page page, PageRule rule) {

        Pattern pattern = Pattern.compile(rule.getChapterDetailUrl());
        Matcher matcher = pattern.matcher(page.getUrl().toString());
        if(matcher.matches()) {
            String novelid = matcher.group(1);
            page.putField("cleanbookname", id2BookNameMap.get(rule.getUid()+"_"+novelid));

            page.putField("chaptercontent", page.getHtml().xpath(rule.getChapterContentXPath()).toString());
            page.putField("chaptertitle", page.getHtml().xpath(rule.getChapterTitleXPath()).toString());
        }
    }

    /**
     * 根据不同操作类型进行提取
     * @param page page
     * @param pointStripRule 提取点规则
     * @return List&lt;String&gt;或者String
     */
    private Object processStrip(Page page, PointStripRule pointStripRule) {

        List<String> params = pointStripRule.getParams();

        switch (pointStripRule.getStripType()) {
            case StripType.HTML_LINKS_REGEX :
                return page.getHtml().links().regex(params.get(0), Integer.parseInt(params.get(1))).all();
            case StripType.HTML_XPATH:
                return page.getHtml().xpath(params.get(0)).toString();
            case StripType.HTML_XPATH_REGEX:
                String str = page.getHtml().xpath(params.get(0)).toString();
                return str.replaceAll(params.get(1), params.get(2));
            case StripType.URL_REGEX:
                return page.getUrl().toString().replaceAll(params.get(0), params.get(1));
            default:
                return null;
        }
    }
}
