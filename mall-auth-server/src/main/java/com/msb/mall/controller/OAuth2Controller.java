package com.msb.mall.controller;

import com.msb.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class OAuth2Controller {

    @RequestMapping("/oauth/weibo/success")
    public String weiboOAuth(@RequestParam("code") String code) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("client_id", "1695943506");
        body.put("client_secret", "5b743e424f08cb6fbe44a28e009ef6c1");
        body.put("grant_type", "authorization_code");
        body.put("redirect_uri", "http://msb.auth.com/oauth/weibo/success");
        body.put("code", code);
        // 根据Code获取对应的Token信息
        HttpResponse post = HttpUtils.doPost("https://api.weibo.com"
                , "/oauth2/access_token"
                , "post"
                , null
                , null
                , body
        );
        // 注册成功就需要跳转到商城的首页
        return "redirect:http://msb.mall.com/home.html";
    }

}