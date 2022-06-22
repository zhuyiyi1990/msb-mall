package com.msb.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.exception.NoStockException;
import com.msb.common.utils.PageUtils;
import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.vo.OrderConfirmVo;
import com.msb.mall.order.vo.OrderResponseVO;
import com.msb.mall.order.vo.OrderSubmitVO;
import com.msb.mall.order.vo.PayVo;

import java.util.Map;

/**
 * 订单
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 15:11:46
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取订单确认页中需要获取的相关信息
     *
     * @return
     */
    OrderConfirmVo confirmOrder();

    OrderResponseVO submitOrder(OrderSubmitVO vo) throws NoStockException;

    PayVo getOrderPay(String orderSn);

    void updateOrderStatus(String orderSn, Integer status);

}