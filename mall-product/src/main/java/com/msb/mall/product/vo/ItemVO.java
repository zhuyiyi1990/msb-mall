package com.msb.mall.product.vo;

import com.msb.mall.product.entity.SkuImagesEntity;
import com.msb.mall.product.entity.SkuInfoEntity;
import com.msb.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * 商品详情页的数据对象
 */
@Data
public class ItemVO {
    // 1.sku的基本信息 pms_sku_info
    SkuInfoEntity info;
    // 2.sku的图片信息pms_sku_images
    List<SkuImagesEntity> images;
    // 3.获取spu中的销售属性的组合
    List<SkuItemSaleAttrVo> saleAttrs;
    // 4.获取SPU的介绍
    SpuInfoDescEntity desc;
    // 5.获取SPU的规格参数
    SpuItemGroupAttrVo baseAttrs;

    @Data
    public static class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class SpuItemGroupAttrVo {
        private String groupName;
        private List<SpuBaseAttrVo> baseAttrs;
    }

    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }
}