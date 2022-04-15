package com.msb.mall.mallsearch.service.impl;

import com.msb.mall.mallsearch.config.MallElasticSearchConfiguration;
import com.msb.mall.mallsearch.service.MallSearchService;
import com.msb.mall.mallsearch.vo.SearchParam;
import com.msb.mall.mallsearch.vo.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        // 1. 准备检索的请求
        SearchRequest request = buildSearchRequest(param);
        try {
            // 2.执行检索操作
            SearchResponse response = client.search(request, MallElasticSearchConfiguration.COMMON_OPTIONS);
            // 3.需要把检索的信息封装为SearchResult
            result = buildSearchResult(response);
        } catch (Exception e) {

        }
        return result;
    }

    private SearchRequest buildSearchRequest(SearchParam param) {
        return null;
    }

    private SearchResult buildSearchResult(SearchResponse response) {
        return null;
    }

}