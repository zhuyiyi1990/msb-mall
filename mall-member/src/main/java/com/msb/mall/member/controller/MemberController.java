package com.msb.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alibaba.fastjson.JSON;
import com.msb.common.exception.BizCodeEnum;
import com.msb.mall.member.exception.PhoneExistException;
import com.msb.mall.member.exception.UsernameExistException;
import com.msb.mall.member.vo.MemberLoginVO;
import com.msb.mall.member.vo.MemberRegisterVO;
import com.msb.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.member.entity.MemberEntity;
import com.msb.mall.member.service.MemberService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;

/**
 * 会员
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 15:10:41
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 会员注册
     *
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVO vo) {
        try {
            memberService.register(vo);
        } catch (UsernameExistException exception) {
            return R.error(BizCodeEnum.USERNAME_EXIST_EXCEPTION.getCode(), BizCodeEnum.USERNAME_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException existException) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (Exception e) {
            return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @RequestMapping("/login")
    public R login(@RequestBody MemberLoginVO vo) {
        MemberEntity entity = memberService.login(vo);
        if (entity != null) {
            return R.ok();
        }
        return R.error(BizCodeEnum.USERNAME_PHONE_VALID_EXCEPTION.getCode(), BizCodeEnum.USERNAME_PHONE_VALID_EXCEPTION.getMsg());
    }

    @RequestMapping("/oauth2/login")
    public R socialLogin(@RequestBody SocialUser vo) {
        MemberEntity entity = memberService.login(vo);
        return R.ok().put("entity", JSON.toJSONString(entity));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);
        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}