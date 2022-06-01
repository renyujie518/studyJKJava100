package org.geekbang.time.commonmistakes.serialization.deserializationconstructor;

import lombok.Data;
/**
 * @description 反序列化时要小心类的构造方法
 */
@Data
public class APIResultWrong {
    private boolean success;
    private int code;

    public APIResultWrong() {
    }

    public APIResultWrong(int code) {
        this.code = code;
        if (code == 2000) success = true;
        else success = false;
    }
}
