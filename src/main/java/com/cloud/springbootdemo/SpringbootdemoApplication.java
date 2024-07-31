package com.cloud.springbootdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 在@EnableAspectJAutoProxy里加一个配置项exposeProxy = true，
 * 表示将代理对象放入到ThreadLocal，这样才可以直接通过 AopContext.currentProxy()的方式获取到
 * @author rayss
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@Slf4j
public class SpringbootdemoApplication implements ApplicationRunner, CommandLineRunner {


    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringbootdemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("ApplicationRunner接口起作用");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("CommandLineRunner接口起作用");
    }
}
