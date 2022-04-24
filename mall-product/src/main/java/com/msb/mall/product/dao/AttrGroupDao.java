package com.msb.mall.product.dao;

import com.msb.mall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.mall.product.vo.SpuItemGroupAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 11:16:48
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemGroupAttrVo> getAttrgroupWithSpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);

}