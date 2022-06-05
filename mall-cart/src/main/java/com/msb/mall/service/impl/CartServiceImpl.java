package com.msb.mall.service.impl;

import com.msb.common.constant.CartConstant;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.feign.ProductFeignService;
import com.msb.mall.interceptor.AuthInterceptor;
import com.msb.mall.service.ICartService;
import com.msb.mall.vo.Cart;
import com.msb.mall.vo.CartItem;
import com.msb.mall.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车信息是存储在Redis中的
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public List<Cart> getCartList() {
        return null;
    }

    /**
     * 把商品添加到购物车中
     *
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> hashOperations = getCartKeyOperation();
        CartItem item = new CartItem();
        // 1.远程调用获取 商品信息
        R r = productFeignService.info(skuId);
        SkuInfoVo vo = (SkuInfoVo) r.get("skuInfo");
        item.setCheck(true);
        item.setCount(num);
        item.setPrice(vo.getPrice());
        item.setImage(vo.getSkuDefaultImg());
        item.setSkuId(skuId);
        item.setTitle(vo.getSkuTitle());
        // 2.获取商品的销售属性
        //item.setSkuAttr();
        return null;
    }

    private BoundHashOperations<String, Object, Object> getCartKeyOperation() {
        // hash key: cart:1   skuId:cartItem
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PREFIX + memberVO.getId();
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }

}