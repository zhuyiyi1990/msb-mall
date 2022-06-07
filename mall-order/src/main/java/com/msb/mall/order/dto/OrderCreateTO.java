package com.msb.mall.order.dto;

import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.entity.OrderItemEntity;
import lombok.Data;

@Data
public class OrderCreateTO {

    private OrderEntity orderEntity; // 订单信息

    private OrderItemEntity orderItemEntity; // 订单信息

}