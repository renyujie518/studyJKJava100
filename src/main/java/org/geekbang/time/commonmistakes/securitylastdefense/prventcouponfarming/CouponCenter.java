package org.geekbang.time.commonmistakes.securitylastdefense.prventcouponfarming;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @description 负责优惠券的产生和发放
 */
@Slf4j
public class CouponCenter {

    AtomicInteger totalSent = new AtomicInteger(0);

    /**
     * @description 调用一次 计数+1
     */
    public void sendCoupon(Coupon coupon) {
        if (coupon != null)
            totalSent.incrementAndGet();
    }

    public int getTotalSentCoupon() {
        return totalSent.get();
    }

    /**
     * @description 没有任何限制，来多少请求生成多少优惠券
     */
    public Coupon generateCouponWrong(long userId, BigDecimal amount) {
        return new Coupon(userId, amount);
    }

    /**
     * @description 每发一次都会从批次中扣除一张 优惠券，发完了就没有了
     */
    public Coupon generateCouponRight(long userId, CouponBatch couponBatch) {
        if (couponBatch.getRemainCount().decrementAndGet() >= 0) {
            return new Coupon(userId, couponBatch.getAmount());
        } else {
            log.info("优惠券批次 {} 剩余优惠券不足", couponBatch.getId());
            return null;
        }
    }

    /**
     * @description 设定了这个批次包含 100 张优惠 券。
     * 保证了一个批次最多只能发放 100 张优惠券
     */
    public CouponBatch generateCouponBatch() {
        CouponBatch couponBatch = new CouponBatch();
        couponBatch.setAmount(new BigDecimal("100"));
        couponBatch.setId(1L);
        couponBatch.setTotalCount(new AtomicInteger(100));
        couponBatch.setRemainCount(couponBatch.getTotalCount());
        couponBatch.setReason("XXX活动");
        return couponBatch;
    }
}
