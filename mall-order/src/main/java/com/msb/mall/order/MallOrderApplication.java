package com.msb.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.msb.mall.order.dao")
@EnableDiscoveryClient
public class MallOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MallOrderApplication.class, args);
	}

}
