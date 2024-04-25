package com.user.service;

// EmailService.java



import org.springframework.cache.CacheManager;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender emailSender;


    private final CacheManager cacheManager; // 注入缓存管理器

    public EmailService(JavaMailSender emailSender,CacheManager cacheManager) {
        this.emailSender = emailSender;
        this.cacheManager = cacheManager;
    }


    public String generateVerificationCode() {
        // 生成 6 位验证码
        Random random = new Random();
        int min = 100000;
        int max = 999999;
        return String.valueOf(random.nextInt(max - min + 1) + min);
    }

    public void sendVerificationCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1310611152@qq.com");
        message.setTo(email);
        message.setSubject("验证码");
        message.setText("您的验证码是：" + code);
        emailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        String cachedCode = cacheManager.getCache("verificationCodeCache").get(email, String.class);
        return code.equals(cachedCode);
    }

    public void cacheVerificationCode(String email, String code) {
        cacheManager.getCache("verificationCodeCache").put(email, code);
    }
}
