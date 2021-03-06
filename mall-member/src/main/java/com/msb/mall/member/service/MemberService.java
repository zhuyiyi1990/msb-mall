package com.msb.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.member.entity.MemberEntity;
import com.msb.mall.member.exception.PhoneExistException;
import com.msb.mall.member.exception.UsernameExistException;
import com.msb.mall.member.vo.MemberLoginVO;
import com.msb.mall.member.vo.MemberRegisterVO;
import com.msb.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 15:10:41
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVO vo) throws PhoneExistException, UsernameExistException;

    MemberEntity login(MemberLoginVO vo);

    /**
     * 社交登录
     *
     * @param vo
     * @return
     */
    MemberEntity login(SocialUser vo);

}