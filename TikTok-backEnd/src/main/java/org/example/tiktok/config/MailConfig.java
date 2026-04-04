package org.example.tiktok.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import com.sendgrid.*;
@Configuration
public class MailConfig {

    @Value("${spring.mail.sendgrid.api-key}")
    private String  sendgridApiKey;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendgridApiKey);
    }


}
