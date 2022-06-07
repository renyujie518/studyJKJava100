package org.geekbang.time.commonmistakes.dataandcode.xss;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
/**
 * @description 自定义一个 Jackson 反列化器，来实现反序列化时的字符串的 HTML 转义
 * 目的是当controller接收@RequestBody时防止注入
 */
public class XssJsonDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = jsonParser.getValueAsString();
        if (value != null) {
            //对于值进行HTML转义
            return HtmlUtils.htmlEscape(value);
        }
        return value;
    }

    @Override
    public Class<String> handledType() {
        return String.class;
    }
}