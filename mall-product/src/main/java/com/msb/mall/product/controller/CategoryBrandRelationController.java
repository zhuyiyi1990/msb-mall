package com.msb.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.product.entity.CategoryBrandRelationEntity;
import com.msb.mall.product.service.CategoryBrandRelationService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;

/**
 * 品牌分类关联
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 11:16:48
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/catelog/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R catelogList(Long brandId) {
//        PageUtils page = categoryBrandRelationService.queryPage(params);
        QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("brand_id", brandId);
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.list(wrapper);
        return R.ok().put("data", list);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);
        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
//        categoryBrandRelationService.save(categoryBrandRelation);
        categoryBrandRelationService.saveDetail(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    @GetMapping("/brands/list")
    public R categorybrandrelation(@RequestParam(value = "catId", required = true, defaultValue = "0") Long catId) {
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.categoryBrandRelation(catId);
        return R.ok().put("data", list);
    }

}