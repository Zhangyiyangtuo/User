package com.user.controller;

import com.user.entity.User;
import com.user.service.EmailService;
import com.user.service.UserService;
import com.user.utils.Result;
import com.user.utils.JwtTokenUtil; // 导入 JwtTokenUtil 类
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EmailService emailService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final JwtTokenUtil jwtTokenUtil; // 添加 JwtTokenUtil 类的引用

    public UserController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public Result<User> loginController(@RequestParam String email, @RequestParam String username, @RequestParam String password) {
        User user = userService.loginService(email, username, password);
        if (user != null) {
            // 登录成功，生成 JWT
            String jwtToken = jwtTokenUtil.generateToken(user.getUid(), user.getUsername());
            return Result.success(jwtToken, user, "success");
        } else {
            return Result.error("1", "邮箱或密码错误");
        }
    }

    @PostMapping("/register")
    public Result<User> registController(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        long result = userService.registService(username, password, email);
        if (result == -1) {
            return Result.error("1", "请输入完整信息！");
        }
        if (result == -2) { 
            return Result.error("1", "用户名已存在！");
        } else {
            return Result.success(null, "success");
        }
    }

    @PostMapping("/send-code")
    public Result<String> sendVerificationCode(@RequestParam String email) {
        String code = emailService.generateVerificationCode();
        emailService.sendVerificationCode(email, code);
        emailService.cacheVerificationCode(email, code); // 缓存验证码
        String token = jwtTokenUtil.generateEmailToken(email);
        return Result.success(token, null,"success");
    }

    @PostMapping("/verify-code")
    public Result<String> verifyCode(@RequestParam String token, @RequestParam String code) {
        String email = jwtTokenUtil.parseEmailToken(token);
        boolean codeVerified = emailService.verifyCode(email, code);
        if (codeVerified) {
            return Result.success("success");
        } else {
            return Result.error("1", "验证码错误！");
        }
    }

    @PostMapping("/modify")
    public Result<String> modifyPassword(@RequestParam String token, @RequestParam String newPassword) {
        String email = jwtTokenUtil.parseToken(token).getSubject();
        boolean result=userService.modifyPassword(email, newPassword);
        if(result==true)
        {
            return Result.success(null,"Password modified successfully");
        }
        else{
            return Result.error("1","未找到对应用户！");
        }

    }
}
