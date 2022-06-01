package org.geekbang.time.commonmistakes.serialization.deserializationconstructor;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @description 15-3 在反序列化的时候，Jackson 框架只会调用无参构 造方法创建对象。
 */
@Data
public class APIResultRight {
    private boolean success;
    private int code;

    public APIResultRight() {
    }

    /**
     * @description 如果走自定义的构造方法创建对象，需要通过 @JsonCreator 来指定构 造方法，
     * 并通过 @JsonProperty 设置构造方法中参数对应的 JSON 属性名
     */
    @JsonCreator
    public APIResultRight(@JsonProperty("code") int code) {
        this.code = code;
        if (code == 2000) success = true;
        else success = false;
    }
}
