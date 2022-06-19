package com.msb.mall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.common.constant.OrderConstant;
import com.msb.common.exception.NoStockException;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.order.dto.OrderCreateTO;
import com.msb.mall.order.entity.OrderItemEntity;
import com.msb.mall.order.feign.CartFeignService;
import com.msb.mall.order.feign.MemberFeignService;
import com.msb.mall.order.feign.ProductService;
import com.msb.mall.order.feign.WareFeignService;
import com.msb.mall.order.interceptor.AuthInterceptor;
import com.msb.mall.order.service.OrderItemService;
import com.msb.mall.order.utils.OrderMsgProducer;
import com.msb.mall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.order.dao.OrderDao;
import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ProductService productService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderMsgProducer orderMsgProducer;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    WareFeignService wareFeignService;

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
        // 在主线程中获取 RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            System.out.println("获取会员:" + Thread.currentThread().getName());
            // RequestContextHolder 绑定主线程中的 RequestAttributes
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1.查询当前登录用户对应的会员的地址信息
            Long id = memberVO.getId();
            List<MemberAddressVo> addresses = memberFeignService.getAddress(id);
            vo.setAddress(addresses);
        }, executor);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            System.out.println("获取购物车:" + Thread.currentThread().getName());
            // RequestContextHolder 绑定主线程中的 RequestAttributes
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2.查询购物车中选中的商品信息
            List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
            vo.setItems(userCartItems);
        }, executor);
        // 主线程需要等待所有的子线程完成后继续
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

    /**
     * 在service中调用自身的其他事务方法的时候，事务的传播行为会失效
     * 因为会绕过代理对象的处理
     */
    @Transactional // 事务A
    public void a() {
        OrderServiceImpl o = (OrderServiceImpl) AopContext.currentProxy();
        o.b(); // 共用事务A
        o.c(); // 事务C
        int a = 10 / 0; // 事务C不会回滚
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void b() {

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void c() {

    }

    /**
     * Seata分布式事务管理 我们需要通过 @GlobalTransactional 修饰
     *
     * @param vo
     * @return
     * @throws NoStockException
     */
    @GlobalTransactional
    @Transactional(/*propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ*/)
    @Override
    public OrderResponseVO submitOrder(OrderSubmitVO vo) throws NoStockException {
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
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                , Arrays.asList(key)
                , vo.getOrderToken());
        if (result == 0) {
            // 表示验证失败 说明是重复提交
            responseVO.setCode(1);
            return responseVO;
        }
        // 2.创建订单和订单项信息
        OrderCreateTO orderCreateTO = createOrder(vo);
        responseVO.setOrderEntity(orderCreateTO.getOrderEntity());
        // 3.保存订单信息
        saveOrder(orderCreateTO);
        // 4.锁定库存信息
        // 订单号  SKU_ID  SKU_NAME 商品数量
        // 封装 WareSkuLockVO 对象
        lockWareSkuStock(responseVO, orderCreateTO);
        // 5.同步更新用户的会员积分
        // int i = 1 / 0;
        // 订单成功后需要给 消息中间件发送延迟30分钟的关单消息
        orderMsgProducer.sendOrderMessage(orderCreateTO.getOrderEntity().getOrderSn());
        return responseVO;
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        // 根据订单号查询相关的订单信息
        OrderEntity orderEntity = this.getBaseMapper().getOrderByOrderSn(orderSn);
        // 通过订单信息封装PayVO对象
        PayVo payVo = new PayVo();
        payVo.setOut_trader_no(orderSn);
        // payVo.setTotal_amount(orderEntity.getTotalAmount()); TODO
        return payVo;
    }

    /**
     * 锁定库存的方法
     *
     * @param responseVO
     * @param orderCreateTO
     * @throws NoStockException
     */
    private void lockWareSkuStock(OrderResponseVO responseVO, OrderCreateTO orderCreateTO) throws NoStockException {
        WareSkuLockVO wareSkuLockVO = new WareSkuLockVO();
        wareSkuLockVO.setOrderSN(orderCreateTO.getOrderEntity().getOrderSn());
        List<OrderItemVo> orderItemVos = orderCreateTO.getOrderItemEntities().stream().map(item -> {
            OrderItemVo itemVo = new OrderItemVo();
            itemVo.setSkuId(item.getSkuId());
            itemVo.setTitle(item.getSkuName());
            itemVo.setCount(item.getSkuQuantity());
            return itemVo;
        }).collect(Collectors.toList());
        wareSkuLockVO.setItems(orderItemVos);
        // 远程锁库存的操作
        R r = wareFeignService.orderLockStock(wareSkuLockVO);
        if (r.getCode() == 0) {
            // 表示锁定库存成功
            responseVO.setCode(0); // 表示 创建订单成功
        } else {
            // 表示锁定库存失败
            responseVO.setCode(2); // 表示库存不足，锁定失败
            throw new NoStockException(10000L);
        }
    }

    /**
     * 生成订单数据
     *
     * @param orderCreateTO
     */
    private void saveOrder(OrderCreateTO orderCreateTO) {
        // 1.订单数据
        OrderEntity orderEntity = orderCreateTO.getOrderEntity();
        this.save(orderEntity);
        // 2.订单项数据
        List<OrderItemEntity> orderItemEntities = orderCreateTO.getOrderItemEntities();
        orderItemService.saveBatch(orderItemEntities);
    }

    /**
     * 创建订单的方法
     *
     * @param vo
     * @return
     */
    private OrderCreateTO createOrder(OrderSubmitVO vo) {
        OrderCreateTO createTO = new OrderCreateTO();
        // 创建订单
        OrderEntity orderEntity = buildOrder(vo);
        createTO.setOrderEntity(orderEntity);
        // 创建OrderItemEntity 订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderEntity.getOrderSn());
        // 根据订单项计算出支付总额
        BigDecimal total_amount = new BigDecimal(0);
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total_amount.add(orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity())));
        }
        createTO.setOrderItemEntities(orderItemEntities);
        return createTO;
    }

    /**
     * 通过购物车中选中的商品来创建对应的购物项信息
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSN) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        // 获取购物车中的商品信息 选中的
        List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
        if (userCartItems != null && userCartItems.size() > 0) {
            // 统一根据SKUID查询出对应的SPU的信息
            List<Long> spuIds = new ArrayList<>();
            for (OrderItemVo orderItemVo : userCartItems) {
                if (!spuIds.contains(orderItemVo.getSpuId())) {
                    spuIds.add(orderItemVo.getSpuId());
                }
            }
            Long[] spuIdsArray = new Long[spuIds.size()];
            spuIdsArray = spuIds.toArray(spuIdsArray);
            System.out.println("---->" + spuIdsArray.length);
            // 远程调用商品服务获取到对应的SPU信息
            List<OrderItemSpuInfoVO> spuInfos = productService.getOrderItemSpuInfoBySpuId(spuIdsArray);
            Map<Long, OrderItemSpuInfoVO> map = spuInfos.stream().collect(Collectors.toMap(OrderItemSpuInfoVO::getId, item -> item));
            for (OrderItemVo userCartItem : userCartItems) {
                // 获取到商品信息对应的 SPU信息
                OrderItemSpuInfoVO spuInfo = map.get(userCartItem.getSpuId());
                OrderItemEntity orderItemEntity = buildOrderItem(userCartItem, spuInfo);
                // 绑定对应的订单编号
                orderItemEntity.setOrderSn(orderSN);
                orderItemEntities.add(orderItemEntity);
            }
        }
        return orderItemEntities;
    }

    /**
     * 根据一个购物车中的商品创建对应的 订单项
     *
     * @param userCartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo userCartItem, OrderItemSpuInfoVO spuInfo) {
        OrderItemEntity entity = new OrderItemEntity();
        // SKU信息
        entity.setSkuId(userCartItem.getSkuId());
        entity.setSkuName(userCartItem.getTitle());
        entity.setSkuPic(userCartItem.getImage());
        entity.setSkuQuantity(userCartItem.getCount());
        List<String> skuAttr = userCartItem.getSkuAttr();
        String skuAttrStr = StringUtils.collectionToDelimitedString(skuAttr, ";");
        entity.setSkuAttrsVals(skuAttrStr);
        // SPU信息
        entity.setSpuId(spuInfo.getId());
        entity.setSpuBrand(spuInfo.getBrandName());
        entity.setCategoryId(spuInfo.getCatalogId());
        entity.setSpuPic(spuInfo.getImg());
        // 优惠信息 忽略
        // 积分信息
        entity.setGiftGrowth(userCartItem.getPrice().intValue());
        entity.setGiftIntegration(userCartItem.getPrice().intValue());
        entity.setSkuPrice(userCartItem.getPrice());
        return entity;
    }

    private OrderEntity buildOrder(OrderSubmitVO vo) {
        // 创建OrderEntity
        OrderEntity orderEntity = new OrderEntity();
        // 创建订单编号
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        // 设置会员相关的信息
        orderEntity.setMemberId(memberVO.getId());
        orderEntity.setMemberUsername(memberVO.getUsername());
        // 根据收货地址ID获取收获地址的详细信息
        MemberAddressVo memberAddressVo = memberFeignService.getAddressById(vo.getAddrId());
        orderEntity.setReceiverCity(memberAddressVo.getCity());
        orderEntity.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
        orderEntity.setReceiverName(memberAddressVo.getName());
        orderEntity.setReceiverPhone(memberAddressVo.getPhone());
        orderEntity.setReceiverPostCode(memberAddressVo.getPostCode());
        orderEntity.setReceiverRegion(memberAddressVo.getRegion());
        orderEntity.setReceiverProvince(memberAddressVo.getProvince());
        // 顶单总额 TODO
        // 设置订单的状态
        orderEntity.setStatus(OrderConstant.OrderStateEnum.FOR_THE_PAYMENT.getCode());
        return orderEntity;
    }

}