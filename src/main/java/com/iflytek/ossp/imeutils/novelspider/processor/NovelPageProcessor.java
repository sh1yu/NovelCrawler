package com.iflytek.ossp.imeutils.novelspider.processor;

import com.iflytek.ossp.imeutils.novelspider.entity.Config;
import com.iflytek.ossp.imeutils.novelspider.entity.ContentStripRule;
import com.iflytek.ossp.imeutils.novelspider.entity.PageType;
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

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    private ContentStripRule rule ;

    public NovelPageProcessor(ContentStripRule rule) {
        this.rule = rule;
    }

    @Override
    public void process(Page page) {

        // TODO: 2016/12/8 应当根据stripRule的Uid选取所需要的stripRule
        ContentStripRule currentRule = rule;

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
    private void processSeedPage(Page page, ContentStripRule rule) {

        List<String> allListUrls = page.getHtml().links().regex(rule.getBookListUrl(), 0).all();
        for(String bookListUrl : allListUrls) {

            if(novelListCount.getAndIncrement() < Config.BOOKLIST_NUMBER_MAXIMUN ) {
                Request request = new Request(bookListUrl);
                request.putExtra("stripRuleUid", rule.getUid());
                request.putExtra("pageType", PageType.BOOKLIST);
                page.addTargetRequest(request);
            }
        }
    }

    /**
     * 处理书籍列表页
     * @param page page
     * @param rule rule
     */
    private void processBookListPage(Page page, ContentStripRule rule) {

        List<String> allInfoUrls = page.getHtml().links().regex(rule.getBookInfoUrl(), 0).all();
        for(String bookInfoUrl : allInfoUrls) {
            if(novelCount.getAndIncrement() < Config.BOOK_NUMBER_MAXIMUN) {
                Request request = new Request(bookInfoUrl);
                request.putExtra("stripRuleUid", rule.getUid());
                request.putExtra("pageType", PageType.BOOKINFO);
                page.addTargetRequest(request);
            }
        }
    }

    /**
     * 处理书籍详情页
     * @param page page
     * @param rule rule
     */
    private void processBookInfoPage(Page page, ContentStripRule rule) {

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
            Set<String> chapterListIdsSet = new HashSet<>();
            chapterListIdsSet.addAll(chapterListIds);

            for(String chapterlisturl : chapterListIdsSet) {
                Pattern chapterlisturlPattern = Pattern.compile(rule.getChapterListUrl());
                Matcher chapterlisturlMatcher = chapterlisturlPattern.matcher(chapterlisturl);
                if(chapterlisturlMatcher.matches()) {
                    String chapterNovelId = matcher.group(1);

                    //找到的链接中，只保存小说ID等于当前页面小说ID的章节列表URL
                    if(novelid.equals(chapterNovelId)) {
                        Request request = new Request(chapterlisturl);
                        request.putExtra("stripRuleUid", rule.getUid());
                        request.putExtra("pageType", PageType.CHAPTERLIST);
                        page.addTargetRequest(request);
                    }
                }
            }
        }
    }

    /**
     * 处理章节列表页
     * @param page page
     * @param rule rule
     */
    private void processChapterListPage(Page page, ContentStripRule rule) {

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
    private void processChapterContentPage(Page page, ContentStripRule rule) {

        Pattern pattern = Pattern.compile(rule.getChapterDetailUrl());
        Matcher matcher = pattern.matcher(page.getUrl().toString());
        if(matcher.matches()) {
            String novelid = matcher.group(1);
            page.putField("cleanbookname", id2BookNameMap.get(rule.getUid()+"_"+novelid));

            page.putField("chaptercontent", page.getHtml().xpath(rule.getChapterContentXPath()).toString());
            page.putField("chaptertitle", page.getHtml().xpath(rule.getChapterTitleXPath()).toString());
        }
    }
}
