package org.geekbang.time.commonmistakes.dataandcode.codeinject;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.HashMap;
import java.util.Map;
/**
 * @description 29-2 动态执行代码时代码注入漏洞
 * 动态执行 JavaScript 代码导致注入漏洞的案例
 *
 */
@RequestMapping("codeinject")
@Slf4j
@RestController
public class CodeInjectController {
    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private ScriptEngine jsEngine = scriptEngineManager.getEngineByName("js");

    //haha';java.lang.System.exit(0);'
    @GetMapping("wrong")
    public Object wrong(@RequestParam("name") String name) {
        try {
            //这里name参数通过字符串拼接方式混入脚本 当外部传入的用 户名为 admin 的时候返回 1，否则返回 0：
            return jsEngine.eval(String.format("var name='%s'; name=='admin'?1:0;", name));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping("right")
    public Object right(@RequestParam("name") String name) {
        try {
            //通过 SimpleBindings 来绑定参数初始化 name 变量，而不是直接拼接代码
            Map<String, Object> parm = new HashMap<>();
            parm.put("name", name);
            return jsEngine.eval("name=='admin'?1:0;", new SimpleBindings(parm));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 使用沙箱执行脚本
     */
    @GetMapping("right2")
    public Object right2(@RequestParam("name") String name) throws InstantiationException {
        ScriptingSandbox scriptingSandbox = new ScriptingSandbox(jsEngine);
        return scriptingSandbox.eval(String.format("var name='%s'; name=='admin'?1:0;", name));
    }
}
