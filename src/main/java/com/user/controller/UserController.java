package com.user.controller;

import com.user.entity.Photo2Album;
import com.user.entity.User;
import com.user.repository.Photo2AlbumDao;
import com.user.service.EmailService;
import com.user.service.UserService;
import com.user.utils.Result;
import com.user.utils.JwtTokenUtil; // 导入 JwtTokenUtil 类
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EmailService emailService;
    @Resource
    private Photo2AlbumDao photo2AlbumDao;

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
        boolean result = userService.updateUserEmailAndName(userid, email, name);
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
//    @PostMapping("/update")
//    public Result updateUserInfo(@RequestParam long userid, @RequestParam String name){
//        boolean result = userService.updateUserInfo(userid, name);
//        if (result) {
//            return Result.success();
//        } else {
//            return Result.error("1", "不存在该用户");
//        }
//    }

// ...

    @PostMapping("/photo/upload")
    public Result uploadPhoto(@RequestParam long userid, @RequestParam MultipartFile photo, @RequestParam(required = false) String photoName, @RequestParam long albumId) {
        try {
            // 获取用户名
            String username = userService.getUsernameById(userid);
            if (username == null) {
                return Result.error("1", "用户不存在");
            }
            // 如果没有提供photoName，使用原始文件名
            if (photoName == null || photoName.isEmpty()) {
                photoName = photo.getOriginalFilename();
            }
            // 构造照片的存储路径
            String photoPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/photo/" + photoName;
            File photoFile = new File(photoPath);
            // 如果文件夹不存在，创建它
            if (!photoFile.getParentFile().exists()) {
                photoFile.getParentFile().mkdirs();
            }
            // 存储照片
            photo.transferTo(photoFile);
            // 将相关信息存入数据库
            Photo2Album photo2Album = new Photo2Album();
            photo2Album.setUid(userid);
            photo2Album.setPhotoname(photoName);
            photo2Album.setAlbumid(albumId);
            photo2AlbumDao.save(photo2Album);
            return Result.success(null, "succeed");
        } catch (Exception e) {
            return Result.error("1", "上传照片失败: " + e.getMessage());
        }
    }
    @GetMapping("/photo/browse")
    public Result browsePhotos(@RequestParam long userid, @RequestParam long albumId) {
        try {
            // 获取用户名
            String username = userService.getUsernameById(userid);
            if (username == null) {
                return Result.error("1", "用户不存在");
            }
            // 查询数据库中所有对应albumId的photoname
            List<Photo2Album> photos = photo2AlbumDao.findByAlbumid(albumId);
            if (photos == null || photos.isEmpty()) {
                return Result.error("1", "相册中没有照片");
            }
            // 构造返回的数据
            List<Map<String, String>> data = new ArrayList<>();
            for (Photo2Album photo : photos) {
                Map<String, String> photoData = new HashMap<>();
                photoData.put("photoname", photo.getPhotoname());
                // 构造照片的URL
                String photoUrl = "photo/"  + photo.getPhotoname();
                photoData.put("photoUrl", photoUrl);
                data.add(photoData);
            }
            return Result.success(data, "success");
        } catch (Exception e) {
            return Result.error("1", "浏览照片失败: " + e.getMessage());
        }
    }
}
