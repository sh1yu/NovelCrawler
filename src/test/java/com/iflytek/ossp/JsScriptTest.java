package com.iflytek.ossp;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/** 测试javascript脚本
 * Created by sypeng on 2016/12/13.
 */
public class JsScriptTest {

    public static void main(String[] args) {

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        try {
            String jscript = "function run() {var s = \"http://book.zongheng.com/quanben/c0/c0/b9/u1/p{X}/v0/s1/t0/ALL.html\";var r = [];for(var i=1; i<=29; i++) {r.push(s.replace(/{X}/g, i));}return r;}";

            System.out.println(jscript);

            engine.eval(jscript);

            //noinspection unchecked
            List<String>  result = (List<String> ) ((Invocable)engine).invokeFunction("run");

            List<String> r = new LinkedList<>();
            //noinspection unchecked
            r.addAll(result);

            System.out.println(r);

        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        System.out.println(File.separator);
        System.out.println(File.pathSeparator);
    }
}


