package com.msb.mall.order.feign;

import com.msb.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("mall-cart")
public interface CartFeignService {

    @GetMapping("/getUserCartItems")
    List<OrderItemVo> getUserCartItems();

}