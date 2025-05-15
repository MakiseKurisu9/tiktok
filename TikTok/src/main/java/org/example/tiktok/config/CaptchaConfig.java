package org.example.tiktok.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    @Bean
    public Producer CaptchaProducer() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "130");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.char.length", "5");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        properties.setProperty("kaptcha.textproducer.char.string", "abcde2345678gfynmnpwx");

        Config config = new Config(properties);
        return config.getProducerImpl();
    }
}
