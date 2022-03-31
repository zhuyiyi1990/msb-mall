package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 11:16:48
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<ProductAttrValueEntity> baseAttrsForSpuId(Long spuId);

}