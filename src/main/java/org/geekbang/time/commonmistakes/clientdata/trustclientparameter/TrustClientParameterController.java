package org.geekbang.time.commonmistakes.clientdata.trustclientparameter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 27-2 客户端提交的参数需要校验
 */
@Slf4j
@RequestMapping("trustclientparameter")
@Controller
@Validated
public class TrustClientParameterController {

    private HashMap<Integer, Country> allCountries = new HashMap<>();

    public TrustClientParameterController() {
        allCountries.put(1, new Country(1, "China"));
        allCountries.put(2, new Country(2, "US"));
        allCountries.put(3, new Country(3, "UK"));
        allCountries.put(4, new Country(4, "Japan"));
    }

    /**
     * @description 设定 注册只支持中国、美国和英国三个国家
     * 因此从数据库中筛选了 id<4 的国家返回给页面进行填充
     *
     * ModelMap对象主要用于传递控制方法处理数据到结果页面，也就是说我们把结果页面上需要的数据放到ModelMap对象中即可，
     * 他的作用类似于request对象的setAttribute方法的作用:用来在一个请求过程中传递处理的数据。
     * ModelMap或者Model通过addAttribute方法向页面传递参数
     */
    @GetMapping("/")
    public String index(ModelMap modelMap) {
        List<Country> countries = new ArrayList<>();
        countries.addAll(allCountries.values().stream().filter(country -> country.getId() < 4).collect(Collectors.toList()));
        modelMap.addAttribute("countries", countries);
        return "index";
    }

    @PostMapping("/wrong")
    @ResponseBody
    public String wrong(@RequestParam("countryId") int countryId) {
        return allCountries.get(countryId).getName();
    }

    /**
     * @description 在使用客户端传过来的参数之前，对参数进行有效性校验：
     */
    @PostMapping("/right")
    @ResponseBody
    public String right(@RequestParam("countryId") int countryId) {
        if (countryId < 1 || countryId > 3)
            throw new RuntimeException("非法参数");
        return allCountries.get(countryId).getName();
    }

    /**
     * @description 使用 Spring Validation 采用注解的方式进行参数校验，更优雅
     * 就是接收>=1  <=3的参数
     */
    @PostMapping("/better")
    @ResponseBody
    public String better(
            @RequestParam("countryId")
            @Min(value = 1, message = "非法参数")
            @Max(value = 3, message = "非法参数") int countryId) {
        return allCountries.get(countryId).getName();
    }
}
