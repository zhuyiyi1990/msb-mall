package com.msb.mall.third;

//import com.aliyun.oss.OSSClient;

import com.msb.mall.third.utils.HttpUtils;
import com.msb.mall.third.utils.SmsComponent;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class MallThirdPartyApplicationTests {

    /*@Autowired
    private OSSClient ossClient;*/

    @Test
    void contextLoads() {
    }

    @Test
    public void testUploadFile() throws FileNotFoundException {
        /*// Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "yourEndpoint";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "yourAccessKeyId";
        String accessKeySecret = "yourAccessKeySecret";
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);*/
        /*InputStream inputStream = new FileInputStream("D:\\localpath\\examplefile.txt");
        ossClient.putObject("examplebucket", "exampledir/exampleobject.txt", inputStream);
        ossClient.shutdown();
        System.out.println("上传图片成功...");*/
    }

    @Autowired
    private SmsComponent component;

    @Test
    public void testSendSMS2() {
        component.sendSmsCode("15021042401", "9966");
    }

    @Test
    public void testSendSMS1() {
        String host = "https://dfsns.market.alicloudapi.com";
        String path = "/data/send_sms";
        String method = "POST";
        String appcode = "52b846ac1f574a2e8a74e2df7585c24c";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:1122");
        bodys.put("phone_number", "15021042401");
        bodys.put("template_id", "TPL_0000");


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}