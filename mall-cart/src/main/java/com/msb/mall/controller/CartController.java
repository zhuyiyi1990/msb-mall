package com.msb.mall.controller;

import com.msb.mall.service.ICartService;
import com.msb.mall.vo.Cart;
import com.msb.mall.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping(value = "/getUserCartItems")
    @ResponseBody
    public List<CartItem> getUserCartItems() {
        return cartService.getUserCartItems();
    }

    @GetMapping("/cart_list")
    public String queryCartList(Model model) {
        Cart cart = cartService.getCartList();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 加入购物车
     *
     * @return
     */
    @GetMapping("/addCart")
    public String addCart(@RequestParam("skuId") Long skuId
            , @RequestParam("num") Integer num
            , Model model) {
        System.out.println("---->addCart");
        CartItem item = null;
        try {
            item = cartService.addCart(skuId, num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("item", item);
        return "success";
    }

}