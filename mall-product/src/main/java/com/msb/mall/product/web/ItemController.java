package com.msb.mall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable Long skuId) {
        // 1.sku的基本信息 pms_sku_info
        // 2.sku的图片信息pms_sku_images
        // 3.获取spu中的销售属性的组合
        // 4.获取SPU的介绍
        // 5.获取SPU的规格参数
        return "item";
    }

}