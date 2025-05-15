package org.example.tiktok.utils;

import org.example.tiktok.dto.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> threadLocal = new ThreadLocal<>();

    public static void saveUser(UserDTO userDTO) { threadLocal.set(userDTO);}

    public static UserDTO getUser() { return threadLocal.get();}
    public static void removeUser() {threadLocal.remove();}
}
