package org.geekbang.time.commonmistakes.serialization.jsonignoreproperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
//加上 @JsonIgnoreProperties 注解，开启 ignoreUnknown 属性，以实现反序列化时忽略额外的数据：
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRight {
    private String name;
}
