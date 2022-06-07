package org.geekbang.time.commonmistakes.dataandcode.xss;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
/**
 * @description 自定义一个 Jackson 列化器，来实现序列化时的字符串的 HTML 转义
 * 目的是如果因为之前的漏洞，数据库中已经保存了一些 JavaScript 代 码，那么读取的时候同样可能出问题。
 * 因此，我们还要实现数据读取的时候也转义
 */
public class XssJsonSerializer extends JsonSerializer<String> {
    @Override
    public Class<String> handledType() {
        return String.class;
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value != null) {
            jsonGenerator.writeString(HtmlUtils.htmlEscape(value));
        }
    }
}