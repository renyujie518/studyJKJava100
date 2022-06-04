package org.geekbang.time.commonmistakes.redundantcode.reflection.right;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @description 把注解和接口 API 序列化为请求需要的字符串的过程
 */
@Slf4j
public class BetterBankService {

    /**
     * @description 每一个接口的实现就非常简单 了，只是参数的组装，然后调用 remoteCall 即可。
     */
    public static String createUser(String name, String identity, String mobile, int age) throws IOException {
        CreateUserAPI createUserAPI = new CreateUserAPI();
        createUserAPI.setName(name);
        createUserAPI.setIdentity(identity);
        createUserAPI.setAge(age);
        createUserAPI.setMobile(mobile);
        return remoteCall(createUserAPI);
    }

    public static String pay(long userId, BigDecimal amount) throws IOException {
        PayAPI payAPI = new PayAPI();
        payAPI.setUserId(userId);
        payAPI.setAmount(amount);
        return remoteCall(payAPI);
    }

    /**
     * @description 反射与注解实现动态的接口参数组装
     * 反射给予了我 们在不知晓类结构的时候，按照固定的逻辑处理类的成员；
     * 而注解给了我们为这些成员补充 元数据的能力，使得我们利用反射实现通用逻辑的时候，可以从外部获得更多我们关心的数据。
     */
    private static String remoteCall(AbstractAPI api) throws IOException {
        //从类上获得了 BankAPI 注解  并从BankAPI注解获取url属性
        BankAPI bankAPI = api.getClass().getAnnotation(BankAPI.class);
        bankAPI.url();
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(api.getClass().getDeclaredFields()) //获取自身的所有字段
                .filter(field -> field.isAnnotationPresent(BankAPIField.class)) //查找标记了注解的字段
                .sorted(Comparator.comparingInt(a -> a.getAnnotation(BankAPIField.class).order())) //根据注解中的order对字段排序
                .peek(field -> field.setAccessible(true)) //设置可以访问私有字段
                .forEach(field -> {   //此时的field是所有标记了@BankAPIField注解的字段（属性）
                    //获得中所有带 BankAPIField 注解
                    BankAPIField bankAPIField = field.getAnnotation(BankAPIField.class);
                    Object value = "";
                    try {
                        //反射获取字段值（属性值）具体的值
                        value = field.get(api);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    //根据字段类型以正确的填充方式格式化字符串
                    switch (bankAPIField.type()) {
                        case "S": {
                            stringBuilder.append(String.format("%-" + bankAPIField.length() + "s", value.toString()).replace(' ', '_'));
                            break;
                        }
                        case "N": {
                            stringBuilder.append(String.format("%" + bankAPIField.length() + "s", value.toString()).replace(' ', '0'));
                            break;
                        }
                        case "M": {
                            if (!(value instanceof BigDecimal))
                                throw new RuntimeException(String.format("{} 的 {} 必须是BigDecimal", api, field));
                            stringBuilder.append(String.format("%0" + bankAPIField.length() + "d", ((BigDecimal) value).setScale(2, RoundingMode.DOWN).multiply(new BigDecimal("100")).longValue()));
                            break;
                        }
                        default:
                            break;
                    }
                });
        //签名逻辑
        stringBuilder.append(DigestUtils.md2Hex(stringBuilder.toString()));
        String param = stringBuilder.toString();
        long begin = System.currentTimeMillis();
        //发请求
        String result = Request.Post("http://localhost:45678/reflection" + bankAPI.url())
                .bodyString(param, ContentType.APPLICATION_JSON)
                .execute().returnContent().asString();
        log.info("调用银行API {} url:{} 参数:{} 耗时:{}ms", bankAPI.desc(), bankAPI.url(), param, System.currentTimeMillis() - begin);
        return result;
    }
}
