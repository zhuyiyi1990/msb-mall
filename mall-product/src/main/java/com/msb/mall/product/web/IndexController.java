package com.msb.mall.product.web;

import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index.html", "/home", "/home.html"})
    public String index(Model model) {
        List<CategoryEntity> list = categoryService.getLevel1Category();
        model.addAttribute("categories", list);
        return "index";
    }

}