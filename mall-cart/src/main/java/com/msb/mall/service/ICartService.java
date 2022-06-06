package com.msb.mall.service;

import com.msb.mall.vo.Cart;
import com.msb.mall.vo.CartItem;

/**
 * 购物车的Service接口
 */
public interface ICartService {

    Cart getCartList();

    CartItem addCart(Long skuId, Integer num) throws Exception;

}