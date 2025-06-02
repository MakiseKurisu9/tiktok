package org.example.tiktok.service.impl;

import com.google.code.kaptcha.Producer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.tiktok.dto.*;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.User.User;
import org.example.tiktok.mapper.LoginMapper;
import org.example.tiktok.service.LoginService;
import org.example.tiktok.utils.EmailValidator;
import org.example.tiktok.utils.JwtUtils;
import org.example.tiktok.utils.PasswordUtils;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.example.tiktok.utils.SystemConstant.*;

@Service
@Slf4j
public class UserServiceImpl implements LoginService {

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

    @Resource
    private LoginMapper loginMapper;

    @Resource
    private JwtUtils jwtUtils;

    //uuid get from front-end
    //非空判断前端使用validator实现
    @Override
    public void getCaptcha(String uuid, HttpServletResponse response) throws IOException {
        try {
            String text = producer.createText();
            System.out.println(text);
            BufferedImage image = producer.createImage(text);
            response.setContentType("image/jpeg");
            response.setHeader("Cache-Control", "no-store, no-cache");
            response.setDateHeader("Expires", 0);
            ImageIO.write(image, "jpg", response.getOutputStream());
            stringRedisTemplate.opsForValue().set(CODE_PREFIX + uuid, text, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("获取验证码失败", e);
        }
    }

    @Override
    public Result login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String code = loginDTO.getCode();
        String uuid = loginDTO.getUuid();
        String getCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX + uuid);
        if( getCode == null || !code.equalsIgnoreCase(getCode) ) {
            return Result.fail("验证码输入错误");
        }
        User user = loginMapper.getUserByUsername(username);
        if(user == null || !passwordUtils.match(password,user.getPassword()) ) {
            return Result.fail("请重新确认输入的是正确的邮箱和密码");
        }
        stringRedisTemplate.delete(CODE_PREFIX + uuid);

        String token = jwtUtils.generateToken(user);
        stringRedisTemplate.opsForValue().set("user:token:"+user.getId(),token,24,TimeUnit.HOURS);

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("token",token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());

        resultMap.put("userInfo",userInfo);

        UserDTO currentUser = new UserDTO(user.getId(),user.getNickname(),user.getEmail());
        UserHolder.saveUser(currentUser);
        System.out.println("login success");
        return Result.ok("登陆成功",resultMap);
    }

    @Override
    public Result findPassword(FindPasswordDTO findPasswordDTO) {
        String code = findPasswordDTO.getCode();
        String email = findPasswordDTO.getEmail();
        String password = findPasswordDTO.getNewPassword();
        String getCode = stringRedisTemplate.opsForValue().get(MAIL_CODE_PREFIX + email);
        if(!emailValidator.isValidEmail(email)) {
            return Result.fail("请确认邮箱格式");
        }
        if(getCode == null || !code.equalsIgnoreCase(getCode)) {
            System.out.println("code wrong");
            return Result.fail("验证码输入错误");
        }
        stringRedisTemplate.delete(MAIL_CODE_PREFIX + email);
        String encodePassword = passwordUtils.encodePassword(password);
        loginMapper.changePassword(encodePassword,email);
        return Result.ok("密码修改成功");
    }

    @Override
    //need TUN mode
    public Result sendMail(EmailCodeDTO emailCodeDTO) {
        //非空判断前端使用validator实现
        String code = emailCodeDTO.getCode();
        String mail = emailCodeDTO.getEmail();
        String uuid = emailCodeDTO.getUuid();
        System.out.println(uuid);
        String getCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX + uuid);
        if(getCode == null || !code.equalsIgnoreCase(getCode) ) {
            return Result.fail("请输入正确的图形验证码");
        }
        if(!emailValidator.isValidEmail(mail) ) {
            System.out.println("fail");
            return Result.fail("请输入正确的邮箱格式");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Login code");
            String mailCode = RandomStringUtils.randomNumeric(6);
            message.setText("Your code is " + mailCode + ", please input in five minutes");
            message.setTo(mail);
            message.setFrom(FROM_MAIL);
            javaMailSender.send(message);
            stringRedisTemplate.opsForValue().set(MAIL_CODE_PREFIX + mail,mailCode,5,TimeUnit.MINUTES);
            return Result.ok("email code send success");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result registry(RegistryDTO registryDTO) {
        String code = registryDTO.getCode();
        String username =registryDTO.getUsername();
        String password = registryDTO.getPassword();
        String email = registryDTO.getEmail();
        String uuid = registryDTO.getUuid();
        String emailCode = registryDTO.getEmailCode();
        String getCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX + uuid);
        if(getCode == null || !code.equalsIgnoreCase(getCode) ) {
            return Result.fail("请输入正确的图形验证码");
        }
        String getEmailCode = stringRedisTemplate.opsForValue().get(MAIL_CODE_PREFIX + email);
        User userByEmail = loginMapper.getUserByUsername(email);
        if(emailCode == null || !emailCode.equalsIgnoreCase(getEmailCode) || userByEmail != null) {
            return Result.fail("邮箱验证码错误或邮箱已被占用");
        }
        stringRedisTemplate.delete(CODE_PREFIX + uuid);
        stringRedisTemplate.delete(MAIL_CODE_PREFIX + email);
        String encodePassword = passwordUtils.encodePassword(password);
        /* save data to db*/
        loginMapper.registry(generateUser(username,encodePassword,email));
        return Result.ok("registry success");
    }

    private User generateUser(String username,String password,String email) {
        String prefix = "user_";
        String number = UUID.randomUUID().toString().replaceAll("-","").substring(0,6);
        String nickname = RandomStringUtils.randomAlphabetic(8);
        User user = new User();
        user.setUsername(username);
        user.setUserDescription("user has not set description");
        user.setNickname(prefix + nickname + "_" + number);
        user.setPassword(password);
        user.setEmail(email);
        user.setAvatarSource(null);
        user.setSex("unknown");
        user.setFollow(0);
        user.setFollowers(0);
        return user;
    }


}
