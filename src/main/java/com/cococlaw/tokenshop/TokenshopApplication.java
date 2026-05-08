package com.cococlaw.tokenshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TokenShop API 启动类
 */
@SpringBootApplication
@MapperScan("com.cococlaw.tokenshop.mapper")
public class TokenshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenshopApplication.class, args);
        System.out.println("===============================================");
        System.out.println("  TokenShop API 服务启动成功!");
        System.out.println("  访问地址: http://localhost:8080/api");
        System.out.println("===============================================");
    }
}
