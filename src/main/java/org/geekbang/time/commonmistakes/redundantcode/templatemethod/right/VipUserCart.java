package org.geekbang.time.commonmistakes.redundantcode.templatemethod.right;

import org.geekbang.time.commonmistakes.redundantcode.templatemethod.Db;
import org.geekbang.time.commonmistakes.redundantcode.templatemethod.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service(value = "VipUserCart")
public class VipUserCart extends NormalUserCart {

    /**
     * @description VIP 用户的购物车 VipUserCart，直接继承了 NormalUserCart，只需要修改多买优惠策 略
     */
    @Override
    protected void processCouponPrice(long userId, Item item) {
        if (item.getQuantity() > 2) {
            item.setCouponPrice(item.getPrice()
                    .multiply(BigDecimal.valueOf(100 - Db.getUserCouponPercent(userId)).divide(new BigDecimal("100")))
                    .multiply(BigDecimal.valueOf(item.getQuantity() - 2)));
        } else {
            item.setCouponPrice(BigDecimal.ZERO);
        }
    }
}
