package com.msb.mall.product.service.impl;

import com.msb.mall.product.service.CategoryBrandRelationService;
import com.msb.mall.product.vo.Catalog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.CategoryDao;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());
        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> queryPageWithTree(Map<String, Object> params) {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> list = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0).map(categoryEntity -> {
                    categoryEntity.setChildren(getCategoryChildren(categoryEntity, categoryEntities));
                    return categoryEntity;
                }).sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public void removeCategoryByIds(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
    }

    private List<CategoryEntity> getCategoryChildren(CategoryEntity categoryEntity, List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(entity -> entity.getParentCid().equals(categoryEntity.getCatId())).map((entity) -> {
                    entity.setChildren(getCategoryChildren(entity, categoryEntities));
                    return entity;
                }).sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catalogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    private List<Long> findParentPath(Long catalogId, List<Long> paths) {
        paths.add(catalogId);
        CategoryEntity entity = this.getById(catalogId);
        if (entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), paths);
        }
        return paths;
    }

    @Override
    @Transactional
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCatalogName(category.getCatId(), category.getName());
        }
    }

    @Override
    public List<CategoryEntity> getLevel1Category() {
        long start = System.currentTimeMillis();
        List<CategoryEntity> list = baseMapper.queryLevel1Category();
        System.out.println("查询消耗的时间：" + (System.currentTimeMillis() - start));
        return list;
    }

    @Override
    public Map<String, List<Catalog2VO>> getCatalog2JSON() {
        List<CategoryEntity> level1Category = this.getLevel1Category();
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            List<CategoryEntity> l2Catalogs = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", value.getCatId()));
            List<Catalog2VO> catalog2VOs = null;
            if (l2Catalogs != null) {
                catalog2VOs = l2Catalogs.stream().map(l2 -> {
                    Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> l3Catalogs = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", catalog2VO.getId()));
                    if (l3Catalogs != null) {
                        List<Catalog2VO.Catalog3VO> catalog3VOS = l3Catalogs.stream().map(l3 -> {
                            Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3VO;
                        }).collect(Collectors.toList());
                        catalog2VO.setCatalog3List(catalog3VOS);
                    }
                    return catalog2VO;
                }).collect(Collectors.toList());
            }
            return catalog2VOs;
        }));
        return map;
    }

}