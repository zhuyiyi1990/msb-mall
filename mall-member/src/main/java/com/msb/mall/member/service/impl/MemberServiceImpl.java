package com.msb.mall.member.service.impl;

import com.msb.mall.member.entity.MemberLevelEntity;
import com.msb.mall.member.exception.PhoneExistException;
import com.msb.mall.member.exception.UsernameExistException;
import com.msb.mall.member.service.MemberLevelService;
import com.msb.mall.member.vo.MemberRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.member.dao.MemberDao;
import com.msb.mall.member.entity.MemberEntity;
import com.msb.mall.member.service.MemberService;

@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );
        return new PageUtils(page);
    }

    /**
     * 完成会员的注册功能
     *
     * @param vo
     */
    @Override
    public void register(MemberRegisterVO vo) {
        MemberEntity entity = new MemberEntity();
        // 设置会员等级 默认值
        MemberLevelEntity memberLevelEntity = memberLevelService.queryMemberLevelDefault();
        entity.setLevelId(memberLevelEntity.getId()); // 设置默认的会员等级

        // 添加对应的账号和手机号是不能重复的
        checkUsernameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());

        entity.setUsername(vo.getUserName());
        entity.setMobile(vo.getPhone());
        // 需要对密码做加密处理
        // entity.setPassword();
        this.save(entity);
    }

    /**
     * 校验手机号是否存在
     *
     * @param phone
     * @throws PhoneExistException
     */
    private void checkPhoneUnique(String phone) throws PhoneExistException {
        int mobile = this.count(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            // 说明手机号是存在的
            throw new PhoneExistException();
        }
    }

    /**
     * 校验账号是否存在
     *
     * @param userName
     * @throws UsernameExistException
     */
    private void checkUsernameUnique(String userName) throws UsernameExistException {
        int username = this.count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username > 0) {
            // 说明手机号是存在的
            throw new UsernameExistException();
        }
    }

}