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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


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
    @GetMapping("/photo/view")
    public ResponseEntity<?> viewPhoto(@RequestParam long userid, @RequestParam String photo) {
        try {
            // 使用userid获取username
            User user = userService.findByUid(userid);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            String username = user.getUsername();

            // 在/username/photo/目录下按照photo名查询
            Path path = Paths.get(System.getProperty("user.home") + "/Desktop/test/" + username + "/photo/" + photo);
            if (!Files.exists(path)) {
                return ResponseEntity.status(404).body("Photo not found");
            }
            File file = path.toFile();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
            String time = sdf.format(file.lastModified());
            String size = Files.size(path) + " bytes";
            String type = Files.probeContentType(path);

            // 使用userid和photo查询albumid
            Photo2Album photo2Album = photo2AlbumDao.findByUidAndPhotoname(userid, photo);
            String albumid = photo2Album == null ? "unknown" : String.valueOf(photo2Album.getAlbumid());

            return ResponseEntity.ok(new PhotoResponse(photo, albumid, size, type, path.toString(), time));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving photo details");
        }
    }
    public class PhotoResponse {
        private String photoname;
        private String albumid;
        private String size;
        private String type;
        private String path;
        private String time;

        public PhotoResponse(String photoname, String albumid, String size, String type, String path, String time) {
            this.photoname = photoname;
            this.albumid = albumid;
            this.size = size;
            this.type = type;
            this.path = path;
            this.time = time;
        }

        public String getPhotoname() {
            return photoname;
        }

        public String getAlbumid() {
            return albumid;
        }

        public String getSize() {
            return size;
        }

        public String getType() {
            return type;
        }

        public String getPath() {
            return path;
        }

        public String getTime() {
            return time;
        }
    }
// ...

    @PutMapping("/photo/manage")
    public ResponseEntity<?> managePhotos(@RequestParam String userid) {
        try {
            // 使用userid获取username
            User user = userService.findByUid(Long.parseLong(userid));
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            String username = user.getUsername();

            // 获取/username/photo/目录下的所有照片
            Path dir = Paths.get(System.getProperty("user.home") + "/Desktop/test/" + username + "/photo/");
            File[] files = dir.toFile().listFiles();
            if (files == null || files.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No photos found");
            }

            // 按修改时间排序
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

            // 构造返回的数据
            List<Map<String, String>> data = new ArrayList<>();
            for (File file : files) {
                Map<String, String> photoData = new HashMap<>();
                photoData.put("photoname", file.getName());
                photoData.put("edit_time", new SimpleDateFormat("dd-MM-yy").format(file.lastModified()));
                data.add(photoData);
            }

            // 构造返回的响应
            Map<String, Object> response = new HashMap<>();
            response.put("error_code", 0);
            response.put("msg", "success");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error managing photos: " + e.getMessage());
        }
    }
}
