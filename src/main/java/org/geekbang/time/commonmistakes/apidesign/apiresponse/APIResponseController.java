package org.geekbang.time.commonmistakes.apidesign.apiresponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

/**
 * @description 22-1  接口响应的编排问题
 */
@Slf4j
@RequestMapping("apiresposne")
@RestController
public class APIResponseController {

    ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("serverNormal")
    public APIResponse<OrderInfo> serverNormal(@RequestParam("userId") Long userId) {
        APIResponse<OrderInfo> response = new APIResponse<>();
        //对于userId为空的情况，收单服务直接处理失败，给予相应的错误码和错误提示
        if (userId == null) {
            response.setSuccess(false);
            response.setCode(3001);
            response.setMessage("Illegal userId");
        } else if (userId == 1) {  //对于userId=1的用户，模拟订单服务对于风险用户的情况
            response.setSuccess(false);
            //把订单服务返回的错误码转换为收单服务错误码
            response.setCode(3002);
            response.setMessage("Internal Error, order is cancelled");
            //同时日志记录内部错误
            log.warn("用户 {} 调用订单服务失败，原因是 Risk order detected", userId);
        } else {
            //其他用户，下单成功
            response.setSuccess(true);
            response.setCode(2000);
            response.setMessage("OK");
            //只有下单成功才有Data
            response.setData(new OrderInfo("Created", 2L));
        }
        return response;
    }

    @GetMapping("server")
    public OrderInfo server(@RequestParam("userId") Long userId) {
        if (userId == null) {
            throw new APIException(3001, "Illegal userId");
        }

        if (userId == 1) {
            //把订单服务的错误包装转换
            //同时日志记录内部错误
            log.warn("用户 {} 调用订单服务失败，原因是 Risk order detected", userId);
            //抛出自定义异常
            throw new APIException(3002, "Internal Error, order is cancelled");
        }
        //直接返回DTO
        return new OrderInfo("Created", 2L);
    }

    @GetMapping("server2")
    public String server2(@RequestParam("userId") Long userId) {
        return userId + "OK";
    }

    @GetMapping("client")
    @NoAPIResponse
    public String client(@RequestParam(value = "error", defaultValue = "0") int error) {
        String url = Arrays.asList("http://localhost:45678/apiresposne/server?userId=2",
                "http://localhost:45678/apiresposne/server2",
                "http://localhost:45678/apiresposne/server?userId=",
                "http://localhost:45678/apiresposne/server?userId=1").get(error);

        //第一层，先看状态码，如果状态码不是200，不处理响应体
        String response = "";
        try {
            //此处是调用上面的serverNormal服务  返回的是APIResponse<OrderInfo>类型的数据的Sring串
            response = Request.Get(url).execute().returnContent().asString();
        } catch (HttpResponseException e) {
            log.warn("请求服务端出现返回非200", e);
            return "服务器忙，请稍后再试！";
        } catch (IOException e) {
            e.printStackTrace();
        }

        //状态码200的情况下处理响应体
        if (!response.equals("")) {
            try {
                //解析响应体
                APIResponse<OrderInfo> apiResponse = objectMapper.readValue(response, new TypeReference<APIResponse<OrderInfo>>() {
                });
                //第二层，success是false直接提示用户
                if (!apiResponse.isSuccess()) {
                    return String.format("创建订单失败，请稍后再试，错误代码： %s 错误原因：%s", apiResponse.getCode(), apiResponse.getMessage());
                } else {
                    //第三层，往下解析data  即OrderInfo
                    OrderInfo orderInfo = apiResponse.getData();
                    if ("Created".equals(orderInfo.getStatus()))
                        return String.format("创建订单成功，订单号是：%s，状态是：%s", orderInfo.getOrderId(), orderInfo.getStatus());
                    else
                        return String.format("创建订单失败，请联系客服处理");
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
