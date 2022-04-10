package com.msb.mall.product.web;

import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.Catalog2VO;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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
        //加锁
//        myLock.lock();
        //通过效果演示我们可以发现，指定了过期时间后那么自动续期就不会生效了，这时我们就需要注意设置的过期时间一定要满足我们的业务场景
        myLock.lock(10, TimeUnit.SECONDS);
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

    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.writeLock();
        String s = null;
        rLock.lock();
        try {
            System.out.println("加写锁成功......");
            s = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set("msg", s);
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    /**
     * 读 读 ： 相当于没有加锁
     * 写 读 ： 需要等待写锁释放
     * 写 写 ： 阻塞的方式
     * 读 写 ： 读数据的时候也会添加锁，那么写的行为也会阻塞
     *
     * @return
     */
    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        String s = null;
        try {
            System.out.println("加读锁成功......");
            s = stringRedisTemplate.opsForValue().get("msg");
            Thread.sleep(30000);
        } catch (Exception e) {
        } finally {
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        try {
            door.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "关门熄灯...";
    }

    @GetMapping("/goHome/{id}")
    @ResponseBody
    public String goHome(@PathVariable Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();
        return id + "下班走人";
    }

    @GetMapping("/park")
    @ResponseBody
    public String park() {
        RSemaphore park = redissonClient.getSemaphore("park");
        boolean b = true;
        try {
//            park.acquire();//获取信号 阻塞到获取成功
            b = park.tryAcquire();//返回获取成功还是失败
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "停车是否成功:" + b;
    }

    @GetMapping("/release")
    @ResponseBody
    public String release() {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();
        return "释放了一个车位";
    }

}