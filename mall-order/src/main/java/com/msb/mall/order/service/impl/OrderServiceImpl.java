package com.msb.mall.order.service.impl;

import com.msb.common.vo.MemberVO;
import com.msb.mall.order.feign.CartFeignService;
import com.msb.mall.order.feign.MemberFeignService;
import com.msb.mall.order.interceptor.AuthInterceptor;
import com.msb.mall.order.vo.MemberAddressVo;
import com.msb.mall.order.vo.OrderConfirmVo;
import com.msb.mall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.order.dao.OrderDao;
import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo vo = new OrderConfirmVo();
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        System.out.println("主线程:" + Thread.currentThread().getName());
        // 获取到 RequestContextHolder 的相关信息
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            System.out.println("获取会员:" + Thread.currentThread().getName());
            // 同步主线程中的 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1.查询当前登录用户对应的会员的地址信息
            Long id = memberVO.getId();
            List<MemberAddressVo> addresses = memberFeignService.getAddress(id);
            vo.setAddress(addresses);
        }, executor);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            System.out.println("获取购物车:" + Thread.currentThread().getName());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2.查询购物车中选中的商品信息
            List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
            vo.setItems(userCartItems);
        }, executor);
        try {
            CompletableFuture.allOf(future1, future2).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 3.计算订单的总金额和需要支付的总金额 VO自动计算
        return vo;
    }

}