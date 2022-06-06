package com.msb.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderWebController {

    @GetMapping("/toTrade")
    public String toTrade() {
        // TODO 查询订单确认页需要的信息
        return "confirm";
    }

}