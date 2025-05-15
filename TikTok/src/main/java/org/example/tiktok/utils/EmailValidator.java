package org.example.tiktok.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailValidator {
    /**
     * 基本邮箱格式的正则表达式
     * 符合RFC 5322标准的邮箱格式
     */
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    /**
     * 更严格的邮箱格式正则表达式
     * 限制了顶级域名的长度和格式
     */
    private static final String STRICT_EMAIL_PATTERN =
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern strictPattern = Pattern.compile(STRICT_EMAIL_PATTERN);

    /**
     * 简单检查邮箱格式是否有效
     *
     * @param email 待验证的邮箱地址
     * @return true 如果邮箱格式有效，否则返回false
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 严格检查邮箱格式是否有效
     *
     * @param email 待验证的邮箱地址
     * @return true 如果邮箱格式有效，否则返回false
     */
    public boolean isValidEmailStrict(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Matcher matcher = strictPattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 检查邮箱是否为常见邮箱服务提供商
     *
     * @param email 待验证的邮箱地址
     * @return true 如果邮箱属于常见服务提供商，否则返回false
     */
    public boolean isCommonEmailProvider(String email) {
        if (!isValidEmail(email)) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        return domain.contains("gmail.com") ||
                domain.contains("yahoo.com") ||
                domain.contains("hotmail.com") ||
                domain.contains("outlook.com") ||
                domain.contains("qq.com") ||
                domain.contains("163.com") ||
                domain.contains("126.com") ||
                domain.contains("foxmail.com");
    }

    /**
     * 获取邮箱的域名部分
     *
     * @param email 邮箱地址
     * @return 邮箱的域名部分，如果邮箱格式无效则返回null
     */
    public String getEmailDomain(String email) {
        if (!isValidEmail(email)) {
            return null;
        }
        return email.substring(email.lastIndexOf("@") + 1);
    }

    /**
     * 获取邮箱的用户名部分
     *
     * @param email 邮箱地址
     * @return 邮箱的用户名部分，如果邮箱格式无效则返回null
     */
    public String getEmailUsername(String email) {
        if (!isValidEmail(email)) {
            return null;
        }
        return email.substring(0, email.lastIndexOf("@"));
    }

    /**
     * 检查邮箱长度是否在合理范围内
     *
     * @param email 待验证的邮箱地址
     * @return true 如果邮箱长度合理，否则返回false
     */
    public boolean isValidEmailLength(String email) {
        // 邮箱地址最大长度通常限制为254个字符
        return email != null && email.length() <= 254 && email.length() >= 3;
    }

    /**
     * 综合检查邮箱格式
     *
     * @param email 待验证的邮箱地址
     * @return true 如果邮箱通过所有检查，否则返回false
     */
    public boolean validateEmail(String email) {
        return isValidEmail(email) && isValidEmailLength(email);
    }
}

