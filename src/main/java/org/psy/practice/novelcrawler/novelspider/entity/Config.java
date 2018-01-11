package org.psy.practice.novelcrawler.novelspider.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** 一些配置
 * Created by sypeng on 2016/12/8.
 */
public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    public static int SPIDER_THREADNUMBER = 1;

    public static int PAGE_DOWNLOAD_TIMEOUT = 5000;
    public static int PAGE_DOWNLOAD_RETRYTIMES = 3;
    public static int PAGE_DOWNLOAD_SLEEPTIME = 500;

    public static int BOOK_IMGURL_TIMEOUT = 5000;

    public static int BOOKLIST_NUMBER_MAXIMUN = 0;
    public static int BOOK_NUMBER_MAXIMUN = 0;

    public static boolean BOOK_STYLE_ENABLE = false;
    public static Map<String, Integer> BOOK_STYLE_NUMBER = new HashMap<>();

    public static boolean CHAPTERLIST_ISUSEVOLUME = true;

    public static String CRAWLSITE ;

    public static String STORAGE_DIR ;

    static {
        Properties properties = new Properties();
        InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(new InputStreamReader(in, Charset.forName("UTF-8")));
        } catch (IOException e) {
            LOGGER.error("加载配置错误");
        }

        try {

            SPIDER_THREADNUMBER = Integer.parseInt(properties.getProperty("spider.thread.number"));

            PAGE_DOWNLOAD_TIMEOUT = Integer.parseInt(properties.getProperty("page.download.timeout"));
            PAGE_DOWNLOAD_RETRYTIMES = Integer.parseInt(properties.getProperty("page.download.retrytimes"));
            PAGE_DOWNLOAD_SLEEPTIME = Integer.parseInt(properties.getProperty("page.download.sleeptime"));
            BOOK_IMGURL_TIMEOUT = Integer.parseInt(properties.getProperty("book.imgurl.timeout"));

            BOOK_NUMBER_MAXIMUN = Integer.parseInt(properties.getProperty("book.number.maximum"));
            BOOKLIST_NUMBER_MAXIMUN = Integer.parseInt(properties.getProperty("bookList.number.maximum"));

            CHAPTERLIST_ISUSEVOLUME = Boolean.parseBoolean(properties.getProperty("book.chapterlist.isUseVolume"));

            CRAWLSITE = properties.getProperty("site.config.name", "");

            STORAGE_DIR = properties.getProperty("storage.dir", "");

            BOOK_STYLE_ENABLE = Boolean.parseBoolean(properties.getProperty("book.style.enable"));

            if(BOOK_STYLE_ENABLE) {
                String[] styles = properties.getProperty("book.style.number").split(";");
                for (String s : styles) {
                    String[] token = s.split(":");
                    BOOK_STYLE_NUMBER.put(token[0], Integer.parseInt(token[1]));
                }
            }

        }catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
