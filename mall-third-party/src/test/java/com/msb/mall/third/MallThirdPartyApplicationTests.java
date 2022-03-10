package com.msb.mall.third;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class MallThirdPartyApplicationTests {

    @Autowired
    private OSSClient ossClient;

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
        InputStream inputStream = new FileInputStream("D:\\localpath\\examplefile.txt");
        ossClient.putObject("examplebucket", "exampledir/exampleobject.txt", inputStream);
        ossClient.shutdown();
        System.out.println("上传图片成功...");
    }

}
