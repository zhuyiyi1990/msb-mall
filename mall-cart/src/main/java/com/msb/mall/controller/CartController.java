package com.msb.mall.controller;

import com.msb.common.vo.MemberVO;
import com.msb.mall.interceptor.AuthInterceptor;
import com.msb.mall.service.ICartService;
import com.msb.mall.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/cart_list")
    public String queryCartList() {
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        System.out.println(memberVO);
        return "cartList";
    }

    /**
     * 加入购物车
     *
     * @return
     */
    @GetMapping("/addCart")
    public String addCart(@RequestParam("skuId") String skuId
            , @RequestParam("num") Integer num
            , Model model) {
        // TODO 把商品添加到购物车中的行为
        System.out.println("---->addCart");
        CartItem item = cartService.addCart(skuId, num);
        model.addAttribute("item", item);
        return "success";
    }

}