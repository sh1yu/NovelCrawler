package com.iflytek.ossp.novelcrawler.novelspider.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** 一些配置
 * Created by sypeng on 2016/12/8.
 */
public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    public static int BOOK_NUMBER_MAXIMUN = 1;
    public static int BOOKLIST_NUMBER_MAXIMUN = 1;

    public static String CRAWLSITE ;

    static {
        Properties properties = new Properties();
        InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            LOGGER.error("加载配置错误");
        }

        BOOK_NUMBER_MAXIMUN = Integer.parseInt(properties.getProperty("book.number.maximum"));
        BOOKLIST_NUMBER_MAXIMUN = Integer.parseInt(properties.getProperty("bookList.number.maximum"));

        CRAWLSITE = properties.getProperty("site.config.name", "");
    }
}
