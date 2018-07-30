package org.psy.crawler.commonutils;

import org.apache.commons.lang.StringUtils;

/** 对url字符串进行处理的工具类
 * Created by sypeng on 2016/10/13.
 */
public class UrlStringUtil {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    /**
     * 获取url去除域名后的文件路径,包含文件名
     * @param url url字符串
     * @return 文件路径
     */
    public static String getUrlPathName(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        String pathname = url;
        if(pathname.startsWith(HTTP)) {
            pathname = pathname.substring(HTTP.length());
        } else if(pathname.startsWith(HTTPS)) {
            pathname = pathname.substring(HTTPS.length());
        }

        String[] token = pathname.split("/", 2);

        if(token.length <= 1) {
            return "";
        } else {
            return token[1];
        }
    }

}
