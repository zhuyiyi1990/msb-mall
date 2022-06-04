package com.msb.mall.controller;

import com.msb.common.constant.SMSConstant;
import com.msb.common.exception.BizCodeEnum;
import com.msb.common.utils.R;
import com.msb.mall.feign.MemberFeignService;
import com.msb.mall.feign.ThirdPartyFeignService;
import com.msb.mall.vo.LoginVo;
import com.msb.mall.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

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
        // 防止60秒之内重复发送
        Object redisCode = redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PREFIX + phone);
        if (redisCode != null) {
            String s = redisCode.toString();
            if (!"".equals(s)) {
                long l = Long.parseLong(redisCode.toString().split("_")[1]);
                if (System.currentTimeMillis() - l <= 60000) {
                    // 说明验证码的发送间隔不足一分钟 提示
                    return R.error(BizCodeEnum.VALID_SMS_EXCEPTION.getCode(), BizCodeEnum.VALID_SMS_EXCEPTION.getMsg());
                }
            }
        }
        // 生成随机的验证码 --》 把生成的验证码存储到Redis服务中 sms:code:15021042401 12345
        String code = UUID.randomUUID().toString().substring(0, 5);
        thirdPartyFeignService.sendSmsCode(phone, code);
        code = code + "_" + System.currentTimeMillis();
        System.out.println("code = " + code);
        redisTemplate.opsForValue().set(SMSConstant.SMS_CODE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        return R.ok();
    }

    @PostMapping("/sms/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, Model model) {
        Map<String, String> map = new HashMap<>();
        if (result.hasErrors()) {
            // 表示提交的数据不合法
            List<FieldError> fieldErrors = result.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                String field = fieldError.getField();
                String defaultMessage = fieldError.getDefaultMessage();
                map.put(field, defaultMessage);
            }
            model.addAttribute("error", map);
            return "/reg";
        } else {
            // 验证码是否正确
            String code = (String) redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PREFIX + vo.getPhone());
            code = code.split("_")[0];
            if (!code.equals(vo.getCode())) {
                // 说明验证码不正确
                map.put("code", "验证码错误");
                model.addAttribute("error", map);
                return "/reg";
            } else {
                // 验证码正确 删除验证码
                redisTemplate.delete(SMSConstant.SMS_CODE_PREFIX + vo.getPhone());
                // 远程调用对应的服务 完成注册功能
                R r = memberFeignService.register(vo);
                if (r.getCode() == 0) {
                    // 注册成功
                    return "redirect:http://auth.msb.com/login.html";
                } else {
                    // 注册失败
                    map.put("msg", r.getCode() + ":" + r.getMessage());
                    model.addAttribute("error", map);
                    return "/reg";
                }
            }
        }
        // return "redirect:/login.html";
    }

    /**
     * 登录的方法
     *
     * @return
     */
    @PostMapping("/login")
    public String login(LoginVo loginVo, RedirectAttributes redirectAttributes,
                        HttpSession session) {
        R r = memberFeignService.login(loginVo);
        if (r.getCode() == 0) {
            session.setAttribute("login", "bobo烤鸭");
            // 表示登录成功
            return "redirect:http://mall.msb.com/home";
        }
        redirectAttributes.addAttribute("errors", r.get("msg"));
        // 表示登录失败，重新跳转到登录页面
        return "redirect:http://auth.msb.com/login.html";
    }

}