package org.geekbang.time.commonmistakes.securitylastdefense.prventcouponfarming;

import lombok.Data;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description 把优惠券看作一种资源，其生产不是凭空的，而是需要事先申请，
 * 申请优惠券批次，批次中包含了固定张数的优惠券、申请原因等信息：
 */
@Data
public class CouponBatch {
    private long id;
    private AtomicInteger totalCount;
    private AtomicInteger remainCount;
    private BigDecimal amount;
    private String reason;
}
