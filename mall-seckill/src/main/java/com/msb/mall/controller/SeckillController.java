package com.msb.mall.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.utils.R;
import com.msb.mall.dto.SeckillSkuRedisDto;
import com.msb.mall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSessionSkus")
    public R getCurrentSeckillSessionSkus() {
        List<SeckillSkuRedisDto> currentSeckillSkus = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", JSON.toJSONString(currentSeckillSkus));
    }

    @GetMapping("/seckillSessionBySkuId")
    public R getSeckillSessionBySkuId(@RequestParam("skuId") Long skuId) {
        SeckillSkuRedisDto dto = seckillService.getSeckillSessionBySkuId(skuId);
        return R.ok().put("data", JSON.toJSONString(dto));
    }

    /**
     * 秒杀抢购
     * killId=1_4&code=82bd0361a5c944eb8344b5a1ef58a628&num=1
     *
     * @return
     */
    @GetMapping("/kill")
    public R seckill(@RequestParam("killId") String killId,
                     @RequestParam("code") String code,
                     @RequestParam("num") Integer num) {
        // 1.校验是否登录 --》通过拦截器去实现
        return R.ok();
    }

}