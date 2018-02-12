package org.psy.practice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 测试正则表达式
 * Created by sypeng on 2016/12/12.
 */
public class RegexTest {

    public static void main(String[] args) {
//        String reg = "http://quanben\\.hongxiu\\.com/free(([0-9]\\.html)|(10\\.html)|(\\.asp\\?page=.*))";
//        String str = "http://quanben.hongxiu.com/free.asp?page=11&dosort=0";

        String reg = "http://book\\.zongheng\\.com/quanben/c0/c0/b9/u1/p[1-9]{1,2}/v0/s1/t0/ALL\\.html";
        String str = "http://book.zongheng.com/quanben/c0/c0/b9/u1/p2/v0/s1/t0/ALL.html";

        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) {
            System.out.println(matcher.group(0));
        } else {
            System.out.println("不匹配！");
        }

        reg = "(?<=架空)?(.{2}).*(小说)?";
        str = "架空历史";
        pattern = Pattern.compile(reg);
        matcher = pattern.matcher(str);
        if(matcher.matches()) {
            System.out.println(matcher.group(1));
        }
    }
}
