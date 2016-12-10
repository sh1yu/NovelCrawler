package com.iflytek.ossp.imeutils.novelspider.entity;

/** 抽取规则类型
 * Created by sypeng on 2016/12/9.
 */
public class StripType {

    public static final String HTML_XPATH = "100"; //html xpath提取， 参数个数为1 xpath
    public static final String HTML_XPATH_REGEX = "101"; //html xpath提取后正则抽取替换，参数个数为3 xpath regex replacement
    public static final String HTML_LINKS_REGEX = "102"; //html所有链接正则匹配 参数个数为2 regex group
    public static final String HTML_XPATH_BLOCK_LISTS = "103"; //html区块列表下的各个元素列表 参数个数为2 xpath1 xpath2

    public static final String URL_REGEX = "200"; //url增则提取， 参数个数为2 regex replacement

    public static final String PAGE_FIELD_REF = "300"; //引用page field参数
}
