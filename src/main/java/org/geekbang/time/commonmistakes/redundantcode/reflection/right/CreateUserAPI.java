package org.geekbang.time.commonmistakes.redundantcode.reflection.right;

import lombok.Data;
//@BankAPI 注解，来补充接口的 URL 和描述等元数据
@BankAPI(url = "/bank/createUser", desc = "创建用户接口")
@Data
public class CreateUserAPI extends AbstractAPI {
    //为每一个字段增加 @BankAPIField 注解，来补充参数的顺序、类型和长度等元数据  注意这里的order需要按照API文档来
    @BankAPIField(order = 1, type = "S", length = 10)
    private String name;
    @BankAPIField(order = 2, type = "S", length = 18)
    private String identity;
    @BankAPIField(order = 4, type = "S", length = 11)
    private String mobile;
    @BankAPIField(order = 3, type = "N", length = 5)
    private int age;
}
