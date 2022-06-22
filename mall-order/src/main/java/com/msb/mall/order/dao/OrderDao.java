package com.msb.mall.order.dao;

import com.msb.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 15:11:46
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    OrderEntity getOrderByOrderSn(@Param("orderSn") String orderSn);

    void updateOrderStatus(@Param("orderSn") String orderSn, @Param("status") Integer status);

}