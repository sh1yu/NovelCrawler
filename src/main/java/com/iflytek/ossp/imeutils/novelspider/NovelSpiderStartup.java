package com.iflytek.ossp.imeutils.novelspider;

import com.iflytek.ossp.imeutils.novelspider.entity.Config;
import com.iflytek.ossp.imeutils.novelspider.entity.PageRule;
import com.iflytek.ossp.imeutils.novelspider.entity.PageType;
import com.iflytek.ossp.imeutils.novelspider.pipeline.NovelDetailUrlPipeline;
import com.iflytek.ossp.imeutils.novelspider.processor.NovelPageProcessor;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

/** 对小说网站进行爬取，生成资源文件夹
 * 每个文件夹以小说名为文件夹名，其中包括如下内容
 * info.json : author bookname description
 * 封面.jpg
 * 章节.json : [{title, index }, ...]
 * 章节内容文件夹
 *
 * Created by sypeng on 2016/12/7.
 */
public class NovelSpiderStartup {

    public static void main(String[] args) {

        PageRule rule = PageRule.create(Config.CRAWLSITE);

        Request seedRequest = new Request(rule.getSeedUrl());
        seedRequest.putExtra("ruleUid", rule.getUid());
        seedRequest.putExtra("pageType", PageType.SEED);

        Spider.create(new NovelPageProcessor(rule))
                .addPipeline(new NovelDetailUrlPipeline())
                .addRequest(seedRequest)
                .thread(Config.SPIDER_THREADNUMBER).run();
    }
}
