package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.msb.common.constant.ProductConstant;
import com.msb.mall.product.dao.AttrAttrgroupRelationDao;
import com.msb.mall.product.dao.AttrGroupDao;
import com.msb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.AttrAttrgroupRelationService;
import com.msb.mall.product.service.AttrGroupService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.AttrGroupRelationVO;
import com.msb.mall.product.vo.AttrResponseVo;
import com.msb.mall.product.vo.AttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.AttrDao;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), new QueryWrapper<AttrEntity>());
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttr(AttrVO vo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(vo, attrEntity);
        this.save(attrEntity);
        if (vo.getAttrGroupId() != null && vo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(vo.getAttrGroupId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBasePage(Map<String, Object> params, Long catalogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ? 1 : 0);
        if (catalogId != 0) {
            wrapper.eq("catalog_id", catalogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> list = records.stream().map((attrEntity) -> {
            AttrResponseVo responseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, responseVo);
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatalogId());
            if (categoryEntity != null) {
                responseVo.setCatalogName(categoryEntity.getName());
            }
            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
                entity.setAttrId(attrEntity.getAttrId());
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrAttrgroupRelationEntity.getAttrGroupId());
                    responseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            return responseVo;
        }).collect(Collectors.toList());
        pageUtils.setList(list);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo responseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, responseVo);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    responseVo.setAttrGroupId(attrGroupEntity.getAttrGroupId());
                    responseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        Long catalogId = attrEntity.getCatalogId();
        Long[] catalogPath = categoryService.findCatalogPath(catalogId);
        responseVo.setCatalogPath(catalogPath);
        CategoryEntity categoryEntity = categoryService.getById(catalogId);
        if (categoryEntity != null) {
            responseVo.setCatalogName(categoryEntity.getName());
        }
        return responseVo;
    }

    @Override
    @Transactional
    public void updateBaseAttr(AttrVO attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr, entity);
        this.updateById(entity);
        if (entity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(entity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    @Override
    @Transactional
    public void removeByIdsDetails(Long[] attrIds) {
        for (Long attrId : attrIds) {
            AttrEntity byId = getById(attrId);
            if (byId != null && byId.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
                attrAttrgroupRelationDao.delete(new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            }
        }
        this.removeByIds(Arrays.asList(attrIds));
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<AttrEntity> attrEntities = list.stream().map((entity) -> this.getById(entity.getAttrId())).filter((entity) -> entity != null).collect(Collectors.toList());
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVO[] vos) {
        List<AttrAttrgroupRelationEntity> list = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, entity);
            return entity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.removeBatchRelation(list);
    }

    @Override
    public PageUtils getNoAttrRelation(Map<String, Object> params, Long attrgroupId) {
// 1.??????????????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
// ????????????????????????id
        Long catalogId = attrGroupEntity.getCatalogId();
// 2.?????????????????????????????????????????????????????????????????????????????????????????????
// ????????????????????????????????????????????????
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catalog_id", catalogId));
// ??????????????????????????????
        List<Long> groupIds = group.stream().map((g) -> g.getAttrGroupId()).collect(Collectors.toList());
// ???????????????????????????????????????????????????????????????????????????
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> attrIds = relationEntities.stream().map((m) -> m.getAttrId()).collect(Collectors.toList());
// ????????????????????????????????????????????????????????????????????????????????????
// ?????????????????????????????????????????????????????????????????? ?????? ???????????????
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catalog_id", catalogId)
// ??????????????????????????????????????????????????????????????????
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
// ???????????????????????????
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
// ????????????key???????????????
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
// ???????????????????????????
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        List<AttrEntity> list = this.list(new QueryWrapper<AttrEntity>().in("attr_id", attrIds).eq("search_type", 1));
        return list.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
    }

}