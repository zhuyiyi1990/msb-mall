package com.msb.mall.controller;

import com.msb.common.constant.AuthConstant;
import com.msb.common.vo.MemberVO;
import com.msb.mall.service.ICartService;
import com.msb.mall.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/cart_list")
    public List<Cart> queryCartList(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(AuthConstant.AUTH_SESSION_REDIS);
        if (attribute != null) {
            MemberVO memberVO = (MemberVO) attribute;
        }
        return null;
    }

}