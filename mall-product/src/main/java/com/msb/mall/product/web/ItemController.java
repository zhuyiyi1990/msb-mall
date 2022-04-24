package com.msb.mall.product.web;

import com.msb.mall.product.service.SkuInfoService;
import com.msb.mall.product.vo.SpuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable Long skuId) {
        SpuItemVO itemVO = skuInfoService.item(skuId);
        return "item";
    }

}