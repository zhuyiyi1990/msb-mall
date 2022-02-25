package com.msb.mall.order.feign;

import com.msb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("mall-product")
public interface ProductService {

    @GetMapping("/product/brand/all")
    R queryAllBrand();

}
