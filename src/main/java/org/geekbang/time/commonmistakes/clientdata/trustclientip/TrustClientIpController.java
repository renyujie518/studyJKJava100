package org.geekbang.time.commonmistakes.clientdata.trustclientip;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

/**
 * @description 27-3 不能信任请求头里的任何内容
 * 随意篡改头的内容
 * 网吧、学校等机构的出口 IP 往往是同一个，在这个场景下，可能只有最先打开这个页面 的用户才能领取到奖品，
 *
 */
@Slf4j
@RequestMapping("trustclientip")
@RestController
public class TrustClientIpController {

    //模拟已发放过奖品的 IP 名单，每次 领取奖品后把 IP 地址加入这个名单中
    HashSet<String> activityLimit = new HashSet<>();

    @GetMapping("test")
    public String test(HttpServletRequest request) {
        String ip = getClientIp(request);
        if (activityLimit.contains(ip)) {
            return "您已经领取过奖品";
        } else {
            activityLimit.add(ip);
            return "奖品领取成功";
        }
    }

    /**
     * @description IP 地址的获取方式是：优先通过 X-ForwardedFor 请求头来获取，
     * 如果没有的话再通过 HttpServletRequest 的 getRemoteAddr 方法来 获取。
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff == null) {
            return request.getRemoteAddr();
        } else {
            return xff.contains(",") ? xff.split(",")[0] : xff;
        }
    }
}
