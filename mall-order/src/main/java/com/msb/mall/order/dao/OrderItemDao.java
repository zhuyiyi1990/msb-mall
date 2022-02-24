package com.msb.mall.order.dao;

import com.msb.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 15:11:46
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
