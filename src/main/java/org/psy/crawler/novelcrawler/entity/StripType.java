package org.psy.crawler.novelcrawler.entity;

/** 抽取规则类型
 * Created by sypeng on 2016/12/9.
 */
public class StripType {

    /*** html xpath提取， 参数个数为1 xpath 返回String*/
    public static final String HTML_XPATH = "100";

    /*** html xpath提取后正则抽取替换，参数个数为3 xpath regex replacement 返回String*/
    public static final String HTML_XPATH_REGEX = "101";

    /*** html所有链接正则匹配 参数个数为2 regex group   返回List<String>*/
    public static final String HTML_LINKS_REGEX = "102";

    /*** html区块列表下的各个元素列表 参数个数为2 xpath1 xpath2 返回List<List<String>>*/
    public static final String HTML_XPATH_BLOCK_LISTS = "103";

    /*** html区块列表下的各个元素的扁平列表 参数个数为2 xpath1 xpath2 返回List<String>*/
    public static final String HTML_XPATH_BLOCK_FLATLIST = "104";

    /*** html xpath提取 List<String>，之后列表转为一个字符串 参数个数为1 xpath 返回String*/
    public static final String HTML_XPATH_LIST_FLATEN = "105";

    /*** html区块列表下的扁平链接列表，之后正则匹配 参数个数为3 xpath1 regex group 返回List<String>*/
    public static final String HTML_XPATH_BLOCK_FLATLINKS_REGEX = "106";



    /*** url增则提取， 参数个数为2 regex replacement 返回String*/
    public static final String URL_REGEX = "200";



    /*** 引用page field参数 返回Object*/
    public static final String PAGE_FIELD_REF = "300";

    /*** 运行没有参数的JS函数, JS无参，配置项两个参数： js functionName 返回字符串list**/
    public static final String RUNJS_NOPARAM_STRLIST = "400";
}
