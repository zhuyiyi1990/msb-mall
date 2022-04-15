package com.msb.mall.mallsearch.controller;

import com.msb.mall.mallsearch.vo.SearchParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @GetMapping("/list.html")
    public String listPage(SearchParam param) {
        return "index";
    }

}