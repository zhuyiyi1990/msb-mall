package com.msb.mall.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.utils.R;
import com.msb.mall.dto.SeckillSkuRedisDto;
import com.msb.mall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}