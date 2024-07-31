package com.cloud.springbootdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootdemoApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        System.out.println(255 + (~5 + 1));
        System.out.println();

    }
}
