package org.geekbang.time.commonmistakes.exception.predefinedexception;

public class Exceptions {

    public static BusinessException ORDEREXISTS = new BusinessException("订单已经存在", 3001);

    /**
     * @description 为避免异常定义为了静态变量，导致异常栈信息错乱
     * 通过不同的方法把每一种异常都 new 出 来抛出即可
     */
    public static BusinessException orderExists() {
        return new BusinessException("订单已经存在", 3001);
    }
}
