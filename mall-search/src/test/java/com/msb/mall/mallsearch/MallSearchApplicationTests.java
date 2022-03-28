package com.msb.mall.mallsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.mall.mallsearch.config.MallElasticSearchConfiguration;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class MallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void contextLoads() {
        System.out.println("--->" + client);
    }

    @Test
    void saveIndex() throws Exception {
        IndexRequest indexRequest = new IndexRequest("system");
        indexRequest.id("1");
//        indexRequest.source("name", "bobokaoya", "age", 18, "gender", "男");
        User user = new User();
        user.setName("bobo");
        user.setAge(22);
        user.setGender("男");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);
        indexRequest.source(json, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User {
        private String name;
        private Integer age;
        private String gender;
    }

    /**
     * 复杂的检索
     */
    @Test
    void searchIndex() throws IOException {
//        1.创建一个SearchRequest对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /*sourceBuilder.query();
        sourceBuilder.from();
        sourceBuilder.size();
        sourceBuilder.aggregation();*/
        searchRequest.source(sourceBuilder);
//        2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
//        3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println("ElasticSearch检索的信息：" + response);
    }

}