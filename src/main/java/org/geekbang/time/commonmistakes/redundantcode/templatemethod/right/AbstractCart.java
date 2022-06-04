package org.geekbang.time.commonmistakes.redundantcode.templatemethod.right;

import org.geekbang.time.commonmistakes.redundantcode.templatemethod.Cart;
import org.geekbang.time.commonmistakes.redundantcode.templatemethod.Db;
import org.geekbang.time.commonmistakes.redundantcode.templatemethod.Item;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * @description 21-1 工厂模式 + 模板方法
 * 把重复的逻辑定义在抽象类中(整个购物车的初始化、统计总价、总运费、总优惠和支 付价格)
 * 三个购物车只要分别实现不同的那份逻辑(不同类型 用户计算运费和优惠的方式不同)
 *
 */
public abstract class AbstractCart {

    public Cart process(long userId, Map<Long, Integer> items) {

        Cart cart = new Cart();

        List<Item> itemList = new ArrayList<>();
        items.entrySet().stream().forEach(entry -> {
            Item item = new Item();
            item.setId(entry.getKey());
            item.setPrice(Db.getItemPrice(entry.getKey()));
            item.setQuantity(entry.getValue());
            itemList.add(item);
        });
        cart.setItems(itemList);

        itemList.stream().forEach(item -> {
            processCouponPrice(userId, item);
            processDeliveryPrice(userId, item);
        });

        cart.setTotalItemPrice(cart.getItems().stream().map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add));
        cart.setTotalDeliveryPrice(cart.getItems().stream().map(Item::getDeliveryPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        cart.setTotalDiscount(cart.getItems().stream().map(Item::getCouponPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        cart.setPayPrice(cart.getTotalItemPrice().add(cart.getTotalDeliveryPrice()).subtract(cart.getTotalDiscount()));
        return cart;
    }

    /**
     * @description 特殊处理的地方留空白也就是留抽象方法定义
     */
    //processCouponPrice 方法用于计算商品折扣
    protected abstract void processCouponPrice(long userId, Item item);

    //processDeliveryPrice 方法用于计算运费
    protected abstract void processDeliveryPrice(long userId, Item item);
}
