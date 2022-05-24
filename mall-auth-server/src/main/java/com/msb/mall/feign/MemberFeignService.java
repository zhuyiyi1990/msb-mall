package com.msb.mall.feign;

import com.msb.common.utils.R;
import com.msb.mall.vo.LoginVo;
import com.msb.mall.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 会员服务
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @RequestMapping("/member/member/login")
    R login(@RequestBody LoginVo vo);

}