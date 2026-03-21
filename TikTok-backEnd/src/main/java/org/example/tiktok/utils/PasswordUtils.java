package org.example.tiktok.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
    public String encodePassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean match(String rawPassword, String encodePassword) {
        return BCrypt.checkpw(rawPassword, encodePassword);
    }
}
