package com.msb.mall.service.impl;

import com.msb.common.utils.R;
import com.msb.mall.feign.CouponFeignService;
import com.msb.mall.service.SeckillService;
import com.msb.mall.vo.SeckillSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public void uploadSeckillSku3Days() {
        // 1. 通过OpenFeign 远程调用Coupon服务中接口来获取未来三天的秒杀活动的商品
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode() == 0) {
            // 表示查询操作成功
            List<SeckillSessionEntity> session = (List<SeckillSessionEntity>) r.get("data");
            // 2. 上架商品
        }
    }

}