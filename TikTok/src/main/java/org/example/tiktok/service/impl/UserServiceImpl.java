package org.example.tiktok.service.impl;

import com.google.code.kaptcha.Producer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.dto.RegistryDTO;
import org.example.tiktok.entity.Result;
import org.example.tiktok.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.example.tiktok.utils.EmailValidator;
import org.example.tiktok.utils.PasswordUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.example.tiktok.utils.SystemConstant.CAPTCHA_PREFIX;
import static org.example.tiktok.utils.SystemConstant.LOGIN_PREFIX;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EmailValidator emailValidator;

    @Resource
    private PasswordUtils passwordUtils;

    @Resource
    private Producer producer;

    //uuid get from front-end
    //非空判断前端使用validator实现
    @Override
    public void getCaptcha(String uuid, HttpServletResponse response) throws IOException {
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        response.setContentType("image/jpeg");
        response.setHeader("Cache-Control", "no-store, no-cache");
        ImageIO.write(image, "jpg", response.getOutputStream());
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + uuid,text,5, TimeUnit.MINUTES);
    }

    @Override
    //need TUN mode
    public Result sendMail(EmailCodeDTO emailCodeDTO) {
        //非空判断前端使用validator实现
        String code = emailCodeDTO.getCode();
        String mail = emailCodeDTO.getEmail();
        String uuid = emailCodeDTO.getUuid();
        String getCode = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + uuid);
        if(!code.equalsIgnoreCase(getCode)) {
            return Result.fail("请输入正确的图形验证码");
        }
        if(!emailValidator.isValidEmail(mail)) {
            return Result.fail("请输入正确的邮箱格式");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Login code");
            String mailCode = RandomStringUtils.randomNumeric(6);
            message.setText("Your code is " + mailCode + ", please input in five minutes");
            message.setTo(mail);
            message.setFrom("kurisumakise195@gmail.com");
            javaMailSender.send(message);
            stringRedisTemplate.opsForValue().set(LOGIN_PREFIX + mail,mailCode,5,TimeUnit.MINUTES);
            return Result.ok("email code send success");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result registry(RegistryDTO registryDTO) {
        String code = registryDTO.getCode();
        String password = registryDTO.getPassword();
        String email = registryDTO.getEmail();
        String uuid = registryDTO.getUuid();
        String emailCode = registryDTO.getEmailCode();
        String getCode = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + uuid);
        if(!code.equals(getCode)) {
            return Result.fail("请输入正确的图形验证码");
        }
        String getEmailCode = stringRedisTemplate.opsForValue().get(LOGIN_PREFIX + email);
        if(!emailCode.equalsIgnoreCase(getEmailCode)) {
            return Result.fail("邮箱验证码错误");
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + uuid);
        stringRedisTemplate.delete(LOGIN_PREFIX + email);
        String encodePassword = passwordUtils.encodePassword(password);
        /*todo save data to db*/
        return Result.ok("registry success");
    }

}
