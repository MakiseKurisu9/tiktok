package org.example.tiktok;

import jakarta.annotation.Resource;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TikTokApplicationTests {

    @Resource
    UserService userService;

    @Test
    void testMail() {
        EmailCodeDTO emailCodeDTO = new EmailCodeDTO("661234","123456","1271892479@qq.com");
        userService.sendMail(emailCodeDTO);
    }

}
