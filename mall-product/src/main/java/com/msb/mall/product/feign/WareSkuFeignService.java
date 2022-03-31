package com.msb.mall.product.feign;

import com.msb.common.dto.SkuHasStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-ware")
public interface WareSkuFeignService {

    @PostMapping("/ware/waresku/hasStock")
    List<SkuHasStockDto> getSkusHasStock(@RequestBody List<Long> skuIds);

}