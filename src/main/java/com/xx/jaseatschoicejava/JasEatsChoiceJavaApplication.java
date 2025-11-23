package com.xx.jaseatschoicejava;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.SpringApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xx.jaseatschoicejava.mapper")
public class JasEatsChoiceJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(JasEatsChoiceJavaApplication.class, args);
    }

}
