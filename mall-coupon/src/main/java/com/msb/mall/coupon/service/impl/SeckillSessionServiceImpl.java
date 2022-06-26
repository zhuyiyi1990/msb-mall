package com.msb.mall.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.coupon.dao.SeckillSessionDao;
import com.msb.mall.coupon.entity.SeckillSessionEntity;
import com.msb.mall.coupon.service.SeckillSessionService;

@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        // 计算未来3天的时间
        return this.list(new QueryWrapper<SeckillSessionEntity>().
                between("start_time", startTime(), endTime()));
    }

    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalDate startDay = now.plusDays(1);
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(startDay, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate endDay = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(endDay, max);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static void main(String[] args) {
        //Date date = new Date(); // 获取的当前日期和时间 2022-06-23 16:10:10
        LocalDate now = LocalDate.now();
        // 获取对应的日期
        System.out.println("now = " + now);
        LocalDate startDay = now.plusDays(1);
        LocalDate endDay = now.plusDays(2);
        System.out.println("startDay = " + startDay);
        System.out.println("endDay = " + endDay);
        // 获取对应的时间
        LocalTime max = LocalTime.MAX;
        LocalTime min = LocalTime.MIN;
        System.out.println("max = " + max);
        System.out.println("min = " + min);
        // 日期和时间的组合
        LocalDateTime start = LocalDateTime.of(startDay, min);
        LocalDateTime end = LocalDateTime.of(endDay, max);
        System.out.println("start = " + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("end = " + end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}