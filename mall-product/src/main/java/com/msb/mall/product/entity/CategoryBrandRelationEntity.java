package com.msb.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * 品牌分类关联
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 11:16:48
 */
@Data
@TableName("pms_category_brand_relation")
public class CategoryBrandRelationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private Long id;

    /**
     * 品牌id
     */
    private Long brandId;

    /**
     * 分类id
     */
    private Long catalogId;

    /**
     *
     */
    private String brandName;

    /**
     *
     */
    private String catalogName;

}