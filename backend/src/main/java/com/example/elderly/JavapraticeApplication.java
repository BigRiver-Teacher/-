package com.example.elderly;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.elderly.mapper")
public class JavapraticeApplication {

    public static void main(String[] args) {

        SpringApplication.run(JavapraticeApplication.class, args);
    }

}
