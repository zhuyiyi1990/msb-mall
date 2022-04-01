package com.msb.mall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/", "/index.html", "/home", "/home.html"})
    public String index() {
        return "index";
    }

}