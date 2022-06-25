package com.msb.mall.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 * 1. @EnableScheduling 开启定时任务
 * 2. @Scheduled 具体开启一个定时任务  通过cron表达式来定时
 */
@Slf4j
@Component
@EnableScheduling
public class SeckillSchedule {

    @Scheduled(cron = "* * * * * *")
    public void schedule() {
        log.info("定时任务.....");
    }

}