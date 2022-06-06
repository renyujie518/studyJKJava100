package org.geekbang.time.commonmistakes.clientdata.trustclientcalculation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
/**
 * @description 27-1 客户端的计算不可信
 */
@Slf4j
@RequestMapping("trustclientcalculation")
@RestController
public class TrustClientCalculationController {
    @PostMapping("/order")
    public void wrong(@RequestBody Order order) {
        this.createOrder(order);
    }

    /**
     * @description  金额问题重新计算
     */
    @PostMapping("/orderRight")
    public void right(@RequestBody Order order) {
        //根据ID重新查询商品
        Item itemFromDB = Db.getItem(order.getItemId());
        //客户端传入的和服务端查询到的商品单价不匹配的时候，给予友好提示
        if (!order.getItemPrice().equals(itemFromDB.getItemPrice())) {
            throw new RuntimeException("您选购的商品价格有变化，请重新下单");
        }
        order.setItemPrice(itemFromDB.getItemPrice());
        //重新计算商品总价
        BigDecimal totalPrice = itemFromDB.getItemPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        if (order.getItemTotalPrice().compareTo(totalPrice) != 0) {
            throw new RuntimeException("您选购的商品总价有变化，请重新下单");
        }
        order.setItemTotalPrice(totalPrice);
        createOrder(order);
    }

    /**
     * @description  让客户端仅传入需要的数据给服务端
     */
    @PostMapping("orderRight2")
    public Order right2(@RequestBody CreateOrderRequest createOrderRequest) {
        //商品ID和商品数量是可信的没问题，其他数据需要由服务端计算
        Item item = Db.getItem(createOrderRequest.getItemId());
        Order order = new Order();
        order.setItemPrice(item.getItemPrice());
        order.setItemTotalPrice(item.getItemPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        createOrder(order);
        return order;
    }

    private void createOrder(Order order) {

    }
}
