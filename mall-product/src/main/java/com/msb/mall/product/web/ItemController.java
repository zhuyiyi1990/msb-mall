package com.msb.mall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable Long skuId) {
        System.out.println(skuId);
        return "item";
    }

}