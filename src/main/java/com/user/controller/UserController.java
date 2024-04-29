package com.user.controller;

import com.user.entity.User;
import com.user.service.EmailService;
import com.user.service.UserService;
import com.user.utils.Result;
import com.user.utils.JwtTokenUtil; // 导入 JwtTokenUtil 类
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

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
    public Result loginController(@RequestParam String email, @RequestParam String username, @RequestParam String password) {
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
    public Result registController(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
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
    public Result verifyCode(@RequestParam String token, @RequestParam String code) {
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


    @GetMapping({"/browse"})
    public Result checkInfoController(@RequestParam String userid, @RequestParam (required = false) String username, @RequestParam (required=false) String password) {
        User user = userService.findByUid(Long.parseLong(userid));
        if (user != null) {
            return Result.success(user, "success");
        } else {
            return Result.error("1", "用户不存在");
        }
    }
    @PostMapping("/update")
    public Result updateUserInfo(@RequestParam long userid, @RequestParam String email, @RequestParam String name){
        boolean result = userService.updateUserInfo(userid, email);
        if (result) {
            return Result.success(null, "success");
        } else {
            return Result.error("1", "不存在该用户");
        }
    }
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestParam long userid, @RequestParam String password, @RequestParam String NewPassword) {
        User user = userService.findByUid(userid);
        if (user == null) {
            return Result.error("1", "不存在该用户");
        } else if (!user.getPassword().equals(password)) {
            return Result.error("1", "密码错误");
        } else {
            boolean result = userService.changePassword(userid, NewPassword);
            if (result) {
                return Result.success(null, "success");
            } else {
                return Result.error("1", "修改失败"); //处理问题或者数据库问题
            }
        }
    }

}
