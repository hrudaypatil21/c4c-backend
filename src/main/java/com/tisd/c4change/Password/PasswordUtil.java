package com.tisd.c4change.Password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public class PasswordUtil {
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // Static methods
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    public static boolean verifyPassword(String password, String hash) {
        return encoder.matches(password, hash);
    }
}