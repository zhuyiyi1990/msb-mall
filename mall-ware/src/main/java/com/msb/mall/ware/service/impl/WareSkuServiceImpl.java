package com.msb.mall.ware.service.impl;

import com.msb.common.dto.SkuHasStockDto;
import com.msb.common.utils.R;
import com.msb.mall.ware.feign.ProductFeignService;
import com.msb.mall.ware.vo.LockStockResult;
import com.msb.mall.ware.vo.OrderItemVo;
import com.msb.mall.ware.vo.WareSkuLockVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.ware.dao.WareSkuDao;
import com.msb.mall.ware.entity.WareSkuEntity;
import com.msb.mall.ware.service.WareSkuService;
import org.springframework.util.StringUtils;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao skuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 查询的条件 skuId=10&wareId=1
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 入库操作
     *
     * @param skuId  商品编号
     * @param wareId 仓库编号
     * @param skuNum 采购商品的数量
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断是否有该商品和仓库的入库记录
        List<WareSkuEntity> list = skuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (list == null || list.size() == 0) {
            // 如果没有就新增商品库存记录
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            try {
                // 动态地设置商品的名称
                R info = productFeignService.info(skuId); // 通过Feign远程调用商品服务的接口
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    entity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            skuDao.insert(entity); // 插入商品库存记录
        } else {
            // 如果有就更新库存
            skuDao.addStock(skuId, wareId, skuNum);
        }
    }

    /**
     * 获取每个skuId对应的库存
     *
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockDto> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockDto> list = skuIds.stream().map(skuId -> {
            Long count = baseMapper.getSkuStock(skuId);
            SkuHasStockDto dto = new SkuHasStockDto();
            dto.setSkuId(skuId);
            dto.setHasStock(count > 0);
            return dto;
        }).collect(Collectors.toList());
        return list;
    }

    /**
     * 锁定库存的操作
     *
     * @param vo
     * @return
     */
    @Override
    public List<LockStockResult> orderLockStock(WareSkuLockVO vo) {
        List<OrderItemVo> items = vo.getItems();
        // 首先找到具有库存的仓库
        List<SkuWareHasStock> collect = items.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(item.getSkuId());
            List<Long> wareIds = this.baseMapper.listHashStock(item.getSkuId());
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());
        // 尝试锁定库存
        return null;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}