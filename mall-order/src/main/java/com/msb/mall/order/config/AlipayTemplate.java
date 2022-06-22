package com.msb.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.msb.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 支付宝的配置文件
 */
//@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    // 商户appid 沙箱账号: bxksvh3341@sandbox.com
    public static String APPID = "2021000121601362";
    // 私钥 pkcs8格式的
    public static String RSA_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+E2aa5HFEOYqzJIjvsyieDaSFVtJFtq+bWAQpMBP9czBVjz8T2l67B5jWBspxZMcPjPccS41n8jeL7MWpen9IPazf1lyuXKKQIoi1h8e4Yr+gzVQluF3QFmehaIOXNggnW93I5xLKcNqPzrqpgl1/a+3atg/kAtses2BdxbKTg9mtt4lpmYZD+njzPo/W6B2rTDw5rX9DSP+r1UvaNiHt91l2eGLypZDk3FPFmIVE5iNk+q63VsuEVY580O6TCWgp0f2sO+ZV4hZTI0hddTaSNYPv5PvqSP3KzHqILysmOQ/Cir1Ro1nyGhsFAyWKizCM3Jaw3CYMB++r4RF+ZZedAgMBAAECggEBAJQ3TKs7t5cbx6wpsR449lNJECFpsk3qRsU7vQLhGrVoPSh6qxUwEu6E3gkHApWpoA/gPFE0GPAVUHssBnZBrz8F2Ogyn9LXKWx7gV8MohCF8CfT9On0+jJpRiX2+aXP3EvUtv0SMvTpS4UYDzZfEclqv+7sSTfjgjpOZba5kqazsJA6rLaGubi8CFjympmgR6uXbZRoIMZGYHYy2zYeC2Cc3XJNUXoNlbJh/R1cwvoOTN6qYjHDh2boElaHaE1OH0xQkkNDCn72Uthxs3nPurz2EebaZfk3SD5cbpkHk+1X1H1Ekodn7hPiVY/vjzVq/Pm7WhfYuZZlP6pu2+XQ/YECgYEA8NhwRdDDO0wK+ehsvgoe3kc6QVplISnEXvVEa53KEM0FKBZQOZJFTOPoIxBUvBNuRY0YUtCKtG5BRitJiUXocCZWOlumkRtDZyBMG+BBeH4mKFwjnBYboh0HdB9jGH7psQ9sL9p0lSyO3XLTyhk242NbpYq/NlrTbJvq/MC8YccCgYEAygko0n+7mf/Xchz1d/hp3L8gHuFrC/Qh2VD/t3mHypppoGNPGIILOjm97ljNUnTeQ5G3ZOXOh2eYPmMIcYppXIzIErl5Nh7iPfPR0vvS0WlBdRSTTcbiz6DDCVj6A6QLjbjsx8FE8VsusyPfXxrLyyJ9J+AgsjiqOOzIXRfYe3sCgYAknCHk2ujyWIrKliQBdzReAWG/kWgR9HFfJdUyrpeKmj+QamPjfHKAT5x3yAG5XRLEC8CIxh5vctsFOYtCDJhag9lToZ7eK9DBGk23Zw1MrZBLTkYzccDdkaASbLr5fEE1ouyFEaXrDwzgaJX9AHwxcGb78O1D6+Z7ee/sX7fC/QKBgE+lhL5XNLHlNh36ZJrDB5jKj/MckoLH34A4elbuRiYM8Y8VF11mBt8RXFrWDztubyDcFRxlrCGdKVuO1wlLs3WU2lrnW22Gh1I9CH7QX6/GetOZJOkIeum3Sqwos4cz6IoaWN32xyMO+z4L6kcn3SdblVNe/ATGafS/A84PVHx5AoGAA1PKmGrBzXLZlWc/UReVH5ht1hzNfzWvjeAySPUjQTp9J7+ug86rlttJIHXK3ErQqAJAs0xNwqEMh5Kh6hhP9nJglHRj/ZBTI38QAiGob4e6Jv2qOpznMy8lTEYPgWBNqBBTX5LpXOAuGC56nPJBfAbzaaSJj0XsSgdDBJEFsjI=";
    // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://order.msb.com/payed/notify";
    // public static String notify_url = "http://localhost:8030/payed/notify";
    // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public static String return_url = "http://order.msb.com/orderPay/returnUrl";
    // public static String return_url = "http://localhost:8030/orderPay/returnUrl";
    // 请求网关地址
    public static String URL = "https://openapi.alipaydev.com/gateway.do";
    // 编码
    public static String CHARSET = "UTF-8";
    // 返回格式
    public static String FORMAT = "json";
    // 支付宝公钥
    public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjrEVFMOSiNJXaRNKicQuQdsREraftDA9Tua3WNZwcpeXeh8Wrt+V9JilLqSa7N7sVqwpvv8zWChgXhX/A96hEg97Oxe6GKUmzaZRNh0cZZ88vpkn5tlgL4mH/dhSr3Ip00kvM4rHq9PwuT4k7z1DpZAf1eghK8Q5BgxL88d0X07m9X96Ijd0yMkXArzD7jg+noqfbztEKoH3kPMRJC2w4ByVdweWUT2PwrlATpZZtYLmtDvUKG/sOkNAIKEMg3Rut1oKWpjyYanzDgS7Cg3awr1KPTl9rHCazk15aNYowmYtVabKwbGVToCAGK+qQ1gT3ELhkGnf3+h53fukNqRH+wIDAQAB";
    // 日志记录目录
    public static String log_path = "/log";
    // RSA2
    public static String SIGNTYPE = "RSA2";

    public String pay(PayVo payVo) {
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        //调用RSA签名方式
        AlipayClient client = new DefaultAlipayClient(URL, APPID, RSA_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGNTYPE);
        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();

        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payVo.getOut_trader_no());
        model.setSubject(payVo.getSubject());
        model.setTotalAmount(payVo.getTotal_amount());
        model.setBody(payVo.getBody());
        model.setTimeoutExpress("5000");
        model.setProductCode("11111");
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(notify_url);
        // 设置同步地址
        alipay_request.setReturnUrl(return_url);

        // form表单生产
        String form = "";
        try {
            // 调用SDK生成表单
            form = client.pageExecute(alipay_request).getBody();
            return form;
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}