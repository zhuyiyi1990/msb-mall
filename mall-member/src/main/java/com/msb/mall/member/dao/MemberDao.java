package com.msb.mall.member.dao;

import com.msb.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 15:10:41
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
