package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.vo.AttrGroupRelationVO;
import com.msb.mall.product.vo.AttrResponseVo;
import com.msb.mall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 11:16:48
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO vo);

    PageUtils queryBasePage(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateBaseAttr(AttrVO attr);

    void removeByIdsDetails(Long[] attrIds);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVO[] vos);

    PageUtils getNoAttrRelation(Map<String, Object> params, Long attrgroupId);

}