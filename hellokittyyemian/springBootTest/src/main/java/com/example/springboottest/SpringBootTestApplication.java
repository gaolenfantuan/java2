package com.example.springboottest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * 运行这个类的 main 方法即可启动服务（默认端口 8080）
 */
@SpringBootApplication
public class SpringBootTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTestApplication.class, args);
        System.out.println("""
                
                ==========================================
                  Hello Kitty 后端启动成功！
                  登录接口  POST http://localhost:8080/api/auth/login
                  注册接口  POST http://localhost:8080/api/auth/register
                  H2 控制台 http://localhost:8080/h2-console
                ==========================================
                """);
    }
}
