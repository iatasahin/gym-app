package dev.ilkersahin.java.spring.gym.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {
    private static final Logger log = LoggerFactory.getLogger(PasswordGeneratorService.class);

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789";

    private SecureRandom secureRandom = new SecureRandom();

    public String generate(int length) {
        log.debug("Generating password of length {}", length);

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(secureRandom.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
