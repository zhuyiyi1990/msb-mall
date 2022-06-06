package com.msb.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认页面的中的数据VO
 */
@Data
public class OrderConfirmVo {
    // 订单的收货人 及 收货地址信息
    List<MemberAddressVo> address;
    // 购物车中选中的商品信息
    List<OrderItemVo> items;
    // 支付方式
    // 发票信息
    // 优惠信息

    BigDecimal total;// 总的金额
    BigDecimal payTotal;// 需要支付的总金额
}