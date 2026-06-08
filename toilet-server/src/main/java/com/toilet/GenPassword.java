package com.toilet;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(args.length > 0 ? args[0] : "admin123");
        System.out.println("BCrypt Hash: " + hash);
    }
}
