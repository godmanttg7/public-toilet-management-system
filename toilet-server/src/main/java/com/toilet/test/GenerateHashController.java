package com.toilet.test;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("dev")
@RequestMapping("/api/test")
public class GenerateHashController {

    @GetMapping("/generate-hash")
    public String generateHash(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        return "Password: " + password + "\nHash: " + hash + "\nHash Length: " + hash.length() + "\n";
    }

    @GetMapping("/verify-hash")
    public String verifyHash(@RequestParam String password, @RequestParam String hash) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(password, hash);
        return "Password: " + password + "\nHash: " + hash + "\nMatches: " + matches + "\n";
    }
}
