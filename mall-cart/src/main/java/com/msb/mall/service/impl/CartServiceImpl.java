package com.msb.mall.service.impl;

import com.msb.mall.service.ICartService;
import com.msb.mall.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车信息是存储在Redis中的
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<Cart> getCartList() {
        return null;
    }

}