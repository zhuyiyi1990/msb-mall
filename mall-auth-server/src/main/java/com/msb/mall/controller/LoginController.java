package com.msb.mall.controller;

import com.msb.common.constant.SMSConstant;
import com.msb.common.utils.R;
import com.msb.mall.feign.ThirdPartyFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    /*@GetMapping("/login.html")
    public String login() {
        return "login";
    }

    @GetMapping("/reg.html")
    public String reg() {
        return "reg";
    }*/

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendSmsCode(@RequestParam("phone") String phone) {
        // 生成随机的验证码 --》 把生成的验证码存储到Redis服务中 sms:code:15021042401 12345
        String code = UUID.randomUUID().toString().substring(0, 5);
        redisTemplate.opsForValue().set(SMSConstant.SMS_CODE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        thirdPartyFeignService.sendSmsCode(phone, code);
        return R.ok();
    }

}