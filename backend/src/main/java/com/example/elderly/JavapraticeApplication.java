package com.example.elderly;

import com.example.elderly.config.LocalEnvBootstrap;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.elderly.mapper")
@EnableScheduling
public class JavapraticeApplication {

    public static void main(String[] args) {
        // 本地开发兜底：未设置 JWT_SECRET / DB_PASSWORD 时，从 backend/.env.local 与
        // backend/.jwt-secret.local 自动补齐（已设置环境变量时不生效）。
        LocalEnvBootstrap.prime();
        SpringApplication.run(JavapraticeApplication.class, args);
    }

}
