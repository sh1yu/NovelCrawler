package com.iflytek.ossp.imeutils.novelspider.utils;

import org.apache.commons.lang.StringUtils;

/** 字符串工具
 * Created by sypeng on 2016/12/8.
 */
public class StringUtil {

    /**
     * 过滤掉不符合文件名规则的字符
     * @param s 原始文件名
     * @return 过滤后的文件名
     */
    public static String filterInvalidFileNameStr(String s) {

        if(StringUtils.isEmpty(s)) {
            return "";
        }
        return s.replace("/", "").replace("\\", "").replace("\"", "")
                .replace("*", "").replace(":", "").replace("?", "")
                .replace(">", "").replace("<", "").replace("|", "").trim();
    }
}
