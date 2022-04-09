package com.msb.mall.product.web;

import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.Catalog2VO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @GetMapping({"/", "/index.html", "/home", "/home.html"})
    public String index(Model model) {
        List<CategoryEntity> list = categoryService.getLevel1Category();
        model.addAttribute("categories", list);
        return "index";
    }

    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCatalog2JSON() {
        Map<String, List<Catalog2VO>> map = categoryService.getCatalog2JSON();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        RLock myLock = redissonClient.getLock("myLock");
        myLock.lock();
        try {
            System.out.println("加锁成功......业务处理......" + Thread.currentThread().getName());
            Thread.sleep(30000);
        } catch (Exception e) {
        } finally {
            System.out.println("释放锁成功....." + Thread.currentThread().getName());
            myLock.unlock();
        }
        return "hello";
    }

}