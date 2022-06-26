package com.msb.mall.service.impl;

import com.msb.common.utils.R;
import com.msb.mall.feign.CouponFeignService;
import com.msb.mall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public void uploadSeckillSku3Days() {
        // 1. 通过OpenFeign 远程调用Coupon服务中接口来获取未来三天的秒杀活动的商品
        R r = couponFeignService.getLatest3DaysSession();
        Object session = r.get("data");
        // 2. 上架商品
    }

}