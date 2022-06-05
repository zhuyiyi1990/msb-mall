package com.msb.mall.controller;

import com.msb.common.vo.MemberVO;
import com.msb.mall.interceptor.AuthInterceptor;
import com.msb.mall.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/cart_list")
    public String queryCartList() {
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        System.out.println(memberVO);
        return "/cartList";
    }

}