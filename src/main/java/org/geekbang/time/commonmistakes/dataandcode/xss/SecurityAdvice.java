package org.geekbang.time.commonmistakes.dataandcode.xss;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.util.HtmlUtils;

import java.beans.PropertyEditorSupport;

/**
 * @description 定义一个 @InitBinder 实现数据绑定的时候，对字符串进行转码
 */
@ControllerAdvice
public class SecurityAdvice {
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        //注册自定义的绑定器
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                Object value = getValue();
                return value != null ? value.toString() : "";
            }

            @Override
            public void setAsText(String text) {
                //赋值时进行HTML转义
                setValue(text == null ? null : HtmlUtils.htmlEscape(text));
            }
        });
    }
}
