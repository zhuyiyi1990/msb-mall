package com.msb.mall.order.service.impl;

import com.msb.common.constant.OrderConstant;
import com.msb.common.vo.MemberVO;
import com.msb.mall.order.feign.CartFeignService;
import com.msb.mall.order.feign.MemberFeignService;
import com.msb.mall.order.interceptor.AuthInterceptor;
import com.msb.mall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @Autowired
    StringRedisTemplate redisTemplate;

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
        // 4.生成防重的Token
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        // 我们需要把这个Token信息存储在Redis中
        // order:token:用户编号
        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId(), token);
        // 然后我们需要将这个Token绑定在响应的数据对象中
        vo.setOrderToken(token);
        return vo;
    }

    // private Lock lock = new ReentrantLock();

    @Override
    public OrderResponseVO submitOrder(OrderSubmitVO vo) {
        // 需要返回响应的对象
        OrderResponseVO responseVO = new OrderResponseVO();
        // 获取当前登录的用户信息
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        // 1.验证是否重复提交  保证Redis中的token 的查询和删除是一个原子性操作
        String key = OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId();
        /*try {
            lock.lock();//加锁
            String redisToken = redisTemplate.opsForValue().get(key);
            if (redisToken != null && redisToken.equals(vo.getOrderToken())) {
                // 表示是第一次提交
                // 需要删除Token
                redisTemplate.delete(key);
            } else {
                // 表示是重复提交
                return responseVO;
            }
        } finally {
            lock.unlock(); //释放锁
        }*/
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0";
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                , Arrays.asList(key)
                , vo.getOrderToken());
        if (result == 0) {
            // 表示验证失败 说明是重复提交
            return responseVO;
        }
        // 是第一次提交 令牌验证成功 开始下订单的操作
        // 2.下订单操作
        return null;
    }

}