package com.msb.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.OrderConstant;
import com.msb.common.constant.SeckillConstant;
import com.msb.common.dto.SeckillOrderDto;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.dto.SeckillSkuRedisDto;
import com.msb.mall.feign.CouponFeignService;
import com.msb.mall.feign.ProductFeignService;
import com.msb.mall.interceptor.AuthInterceptor;
import com.msb.mall.service.SeckillService;
import com.msb.mall.vo.SeckillSessionEntity;
import com.msb.mall.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void uploadSeckillSku3Days() {
        // 1. 通过OpenFeign 远程调用Coupon服务中接口来获取未来三天的秒杀活动的商品
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode() == 0) {
            // 表示查询操作成功
            String json = (String) r.get("data");
            List<SeckillSessionEntity> seckillSessionEntities = JSON.parseArray(json, SeckillSessionEntity.class);
            // 2. 上架商品  Redis数据保存
            // 缓存商品
            // 2.1 缓存每日秒杀的SKU基本信息
            saveSessionInfos(seckillSessionEntities);
            // 2.2 缓存每日秒杀的商品信息
            saveSessionSkuInfos(seckillSessionEntities);
        }
    }

    /**
     * 查询出当前时间内的秒杀活动及对应的商品SKU信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisDto> getCurrentSeckillSkus() {
        // 1.确定当前时间是属于哪个秒杀活动的
        long time = new Date().getTime();
        // 从Redis中查询所有的秒杀活动
        Set<String> keys = redisTemplate.keys(SeckillConstant.SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            //seckill:sessions1656554400000_1656556200000
            String replace = key.replace(SeckillConstant.SESSION_CACHE_PREFIX, "");
            // 1656554400000_1656556200000
            String[] s = replace.split("_");
            Long start = Long.parseLong(s[0]); // 活动开始的时间
            Long end = Long.parseLong(s[1]); // 活动结束的时间
            if (time > start && time < end) {
                // 说明当前的秒杀活动就是当前时间需要参与的活动
                // 取出来的是SKU的ID  2_9
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
                List<String> list = ops.multiGet(range);
                if (list != null && list.size() > 0) {
                    List<SeckillSkuRedisDto> collect = list.stream().map(item -> {
                        SeckillSkuRedisDto seckillSkuRedisDto = JSON.parseObject(item, SeckillSkuRedisDto.class);
                        return seckillSkuRedisDto;
                    }).collect(Collectors.toList());
                    return collect;
                }
            }
        }
        return null;
    }

    /**
     * 根据SKUID查询秒杀活动对应的信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisDto getSeckillSessionBySkuId(Long skuId) {
        // 1.找到所有需要参与秒杀的商品的sku信息
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            String regex = "\\d_" + skuId; // 2_1
            for (String key : keys) {
                boolean matches = Pattern.matches(regex, key);
                if (matches) {
                    // 说明找到了对应的SKU的信息
                    String json = ops.get(key);
                    SeckillSkuRedisDto dto = JSON.parseObject(json, SeckillSkuRedisDto.class);
                    return dto;
                }
            }
        }
        return null;
    }

    /**
     * 实现秒杀逻辑
     *
     * @param killId
     * @param code
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String code, Integer num) {
        // 1.根据killId获取当前秒杀的商品的信息  Redis中
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
        String json = ops.get(killId);
        if (StringUtils.isNotBlank(json)) {
            SeckillSkuRedisDto dto = JSON.parseObject(json, SeckillSkuRedisDto.class);
            // 校验合法性  1.校验时效性
            Long startTime = dto.getStartTime();
            Long endTime = dto.getEndTime();
            long now = new Date().getTime();
            if (now > startTime && now < endTime) {
                // 说明是在秒杀活动时间范围内的请求
                // 2.校验 随机码和商品 是否合法
                String randCode = dto.getRandCode();
                Long skuId = dto.getSkuId();
                String redisKillId = dto.getPromotionSessionId() + "_" + skuId;
                if (randCode.equals(code) && killId.equals(redisKillId)) {
                    // 随机码校验合法
                    // 3.判断抢购商品数量是否合法
                    if (num <= dto.getSeckillLimit().intValue()) {
                        // 满足限购的条件
                        // 4.判断是否满足 幂等性
                        // 只要抢购成功我们就在Redis中 存储一条信息 userId + sessionId + skuId
                        MemberVO memberVO = AuthInterceptor.threadLocal.get();
                        Long id = memberVO.getId();
                        String redisKey = id + "_" + redisKillId;
                        Boolean aBoolean = redisTemplate.opsForValue()
                                .setIfAbsent(redisKey, num.toString(), (endTime - now), TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            // 表示数据插入成功 是第一次操作
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randCode);
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if (b) {
                                    // 表示秒杀成功
                                    String orderSN = UUID.randomUUID().toString().replace("-", "");
                                    // 继续完成快速下订单操作  --> RocketMQ
                                    SeckillOrderDto orderDto = new SeckillOrderDto();
                                    orderDto.setOrderSN(orderSN);
                                    orderDto.setSkuId(skuId);
                                    orderDto.setSeckillPrice(dto.getSeckillPrice());
                                    orderDto.setMemberId(id);
                                    orderDto.setNum(num);
                                    orderDto.setPromotionSessionId(dto.getPromotionSessionId());
                                    // 通过RocketMQ 发送异步消息
                                    rocketMQTemplate.sendOneWay(OrderConstant.ROCKETMQ_SECKILL_ORDER_TOPIC
                                            , JSON.toJSONString(orderDto));
                                    return orderSN;
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 保存每日活动的信息到Redis中
     *
     * @param seckillSessionEntities
     */
    private void saveSessionInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        for (SeckillSessionEntity seckillSessionEntity : seckillSessionEntities) {
            // 循环缓存每一个活动  key： start_endTime
            long start = seckillSessionEntity.getStartTime().getTime();
            long end = seckillSessionEntity.getEndTime().getTime();
            // 生成Key
            String key = SeckillConstant.SESSION_CACHE_PREFIX + start + "_" + end;
            Boolean flag = redisTemplate.hasKey(key);
            if (!flag) {// 表示这个秒杀活动在Redis中不存在，也就是还没有上架，那么需要保存
                // 需要存储到Redis中的这个秒杀活动涉及到的相关的商品信息的SKUID
                List<String> collect = seckillSessionEntity.getRelationEntities().stream().map(item -> {
                    // 秒杀活动存储的 VALUE是 sessionId_SkuId
                    return item.getPromotionSessionId() + "_" + item.getSkuId().toString();
                }).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        }
    }

    /**
     * 存储活动对应的 SKU信息
     *
     * @param seckillSessionEntities
     */
    private void saveSessionSkuInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        seckillSessionEntities.stream().forEach(session -> {
            // 循环取出每个Session，然后取出对应SkuID 封装相关的信息
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SeckillConstant.SKU_CACHE_PREFIX);
            session.getRelationEntities().stream().forEach(item -> {
                String skuKey = item.getPromotionSessionId() + "_" + item.getSkuId();
                // Boolean flag = redisTemplate.hasKey(skuKey);
                Boolean flag = hashOps.hasKey(skuKey);
                if (!flag) {
                    SeckillSkuRedisDto dto = new SeckillSkuRedisDto();
                    // 1.获取SKU的基本信息
                    R info = productFeignService.info(item.getSkuId());
                    if (info.getCode() == 0) {
                        // 表示查询成功
                        String json = (String) info.get("skuInfoJSON");
                        dto.setSkuInfoVo(JSON.parseObject(json, SkuInfoVo.class));
                    }
                    // 2.获取SKU的秒杀信息
                    /*dto.setSkuId(item.getSkuId());
                    dto.setSeckillPrice(item.getSeckillPrice());
                    dto.setSeckillCount(item.getSeckillCount());
                    dto.setSeckillLimit(item.getSeckillLimit());
                    dto.setSeckillSort(item.getSeckillSort());*/
                    BeanUtils.copyProperties(item, dto);
                    // 3.设置当前商品的秒杀时间
                    dto.setStartTime(session.getStartTime().getTime());
                    dto.setEndTime(session.getEndTime().getTime());
                    // 4. 随机码
                    String token = UUID.randomUUID().toString().replace("-", "");
                    dto.setRandCode(token);
                    // 绑定对应的 活动编号
                    dto.setPromotionSessionId(item.getPromotionSessionId());
                    // 分布式信号量的处理  限流的目的
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                    // 把秒杀活动的商品数量作为分布式信号量的信号量
                    semaphore.trySetPermits(item.getSeckillCount().intValue());
                    hashOps.put(skuKey, JSON.toJSONString(dto));
                }
            });
        });
    }

}