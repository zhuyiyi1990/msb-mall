package com.msb.mall.service.impl;

import com.msb.common.constant.SeckillConstant;
import com.msb.common.utils.R;
import com.msb.mall.feign.CouponFeignService;
import com.msb.mall.service.SeckillService;
import com.msb.mall.vo.SeckillSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public void uploadSeckillSku3Days() {
        // 1. 通过OpenFeign 远程调用Coupon服务中接口来获取未来三天的秒杀活动的商品
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode() == 0) {
            // 表示查询操作成功
            List<SeckillSessionEntity> seckillSessionEntities = (List<SeckillSessionEntity>) r.get("data");
            // 2. 上架商品  Redis数据保存
            // 缓存商品
            // 2.1 缓存每日秒杀的SKU基本信息
            saveSessionInfos(seckillSessionEntities);
            // 2.2 缓存每日秒杀的商品信息
            saveSessionSkuInfos(seckillSessionEntities);
        }
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
            // 需要存储到Redis中的这个秒杀活动涉及到的相关的商品信息的SKUID
            List<String> collect = seckillSessionEntity.getRelationEntities().stream().map(item -> {
                return item.getSkuId().toString();
            }).collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(key, collect);
        }
    }

    /**
     * 存储活动对应的 SKU信息
     *
     * @param seckillSessionEntities
     */
    private void saveSessionSkuInfos(List<SeckillSessionEntity> seckillSessionEntities) {
    }

}