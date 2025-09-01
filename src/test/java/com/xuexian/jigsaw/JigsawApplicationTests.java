package com.xuexian.jigsaw;

import com.xuexian.jigsaw.util.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JigsawApplicationTests {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Test
    void test() {

        System.out.println(passwordEncoder.encode("10000"));
    }

}
