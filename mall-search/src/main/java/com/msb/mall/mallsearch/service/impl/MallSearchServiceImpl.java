package com.msb.mall.mallsearch.service.impl;

import com.msb.mall.mallsearch.config.MallElasticSearchConfiguration;
import com.msb.mall.mallsearch.constant.ESConstant;
import com.msb.mall.mallsearch.service.MallSearchService;
import com.msb.mall.mallsearch.vo.SearchParam;
import com.msb.mall.mallsearch.vo.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    /**
     * 构建检索的请求
     * 模糊匹配，关键字匹配
     * 过滤(类别，品牌，属性，价格区间，库存)
     * 排序
     * 分页
     * 高亮
     * 聚合分析
     *
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(ESConstant.PRODUCT_INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 构建具体的检索的条件
        // 1.构建bool查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 关键字的条件
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("subTitle", param.getKeyword()));
        }
        // 1.2 类别的检索条件
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 1.3 品牌的检索条件
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.4 是否有库存
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 1.5 根据价格区间来检索
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            String[] msg = param.getSkuPrice().split("_");
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            if (msg.length == 2) {
                // 说明是 200_300
                skuPrice.gte(msg[0]);
                skuPrice.lte(msg[1]);
            } else if (msg.length == 1) {
                // 说明是 _300  200_
                if (param.getSkuPrice().endsWith("_")) {
                    // 说明是 200_
                    skuPrice.gte(msg[0]);
                }
                if (param.getSkuPrice().startsWith("_")) {
                    // 说明是 _300
                    skuPrice.lte(msg[0]);
                }
            }
            boolQuery.filter(skuPrice);
        }
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    private SearchResult buildSearchResult(SearchResponse response) {
        return null;
    }

}