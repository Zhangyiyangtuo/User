package com.user.controller;

import com.user.entity.MyFile;
import com.user.entity.Share;
import com.user.service.ShareService;
import com.user.service.UserService;
import com.user.service.serviceImpl.FileServiceImpl;
import com.user.utils.JwtTokenUtil;
import com.user.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import  java.io.File;
import org.apache.commons.io.FileUtils;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileServiceImpl fileService;
    @Autowired
    private UserService userService;
    @Autowired
    private ShareService shareService;

    private final JwtTokenUtil jwtTokenUtil; // 添加 JwtTokenUtil 类的引用

    public FileController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/getAll")
    public ResponseEntity<Object> getAllFiles(@RequestParam(required = false) Long userid,
                                              @RequestParam int sortord,
                                              @RequestParam int order) {
        List<MyFile> myFiles = fileService.getAllFiles(userid, sortord, order);
        return ResponseEntity.ok(myFiles);
    }

@PostMapping("/add")
public Result addFile(@RequestParam long userid,
                      @RequestParam("file") MultipartFile file,
                      @RequestParam("file_path") String filePath) {
    try {
        // 获取文件名
        String filename = file.getOriginalFilename();
        // 获取文件大小
        String size = file.getSize() + " bytes";
        // 获取用户名
        String username = userService.getUsernameById(userid);
        if (username == null) {
            return Result.error("1", "添加文件失败");
        }
        // 解析文件路径并在用户目录下创建对应的文件夹
        String userFolderPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + filePath;
        File userFolder = new File(userFolderPath);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }
        // 保存文件到创建的文件夹中
        String fileUrl = userFolderPath + "/" + filename;
        file.transferTo(new File(fileUrl));
        // 调用你的服务来添加文件
//        boolean result = fileService.addFile(userid, filename, size, fileUrl);
//        if (result) {
//            return Result.success(null, "success");
//        } else {
//            return Result.error("1", "添加文件失败");
//        }
        return Result.success(null, "success");
    } catch (IOException e) {
        e.printStackTrace();
        return Result.error("1", "添加文件失败");
    }

}
    @PostMapping("/move")
    public Result moveFile(@RequestParam long userid, @RequestParam String prev_path, @RequestParam String new_path) {
        boolean result = fileService.moveFile(userid, prev_path, new_path);
        if (result) {
            return Result.success(null, "success");
        } else {
            return Result.error("1", "移动文件失败");
        }
    }
    @PostMapping("/rename")
    public Result renameFile(@RequestParam long userid, @RequestParam String file_path, @RequestParam String new_name) {
        boolean result = fileService.renameFile(userid, file_path, new_name);
        if (result) {
            return Result.success(null, "success");
        } else {
            return Result.error("1", "重命名文件失败");
        }
    }
    @PostMapping("/count")
    public Result countFiles(@RequestParam(required = false) Long userid) {
        int count = fileService.countFiles(userid);
        if (count >= 0) {
            return Result.success(Collections.singletonMap("count", count), "success");
        } else {
            return Result.error("1", "获取文件数量失败");
        }
    }
    @PostMapping("/update")
    public Result updateFile(@RequestParam long userid, @RequestParam String file_path, @RequestParam String file_url) {
        boolean result = fileService.updateFile(userid, file_path, file_url);
        if (result) {
            return Result.success(null, "success");
        } else {
            return Result.error("1", "更新文件失败");
        }
    }
    @PostMapping("/search")
    public ResponseEntity<List<MyFile>> searchFiles(@RequestParam String userid,
                                                    @RequestParam String filename) {
        List<MyFile> myFiles = fileService.searchFiles(userid, filename);
        return ResponseEntity.ok(myFiles);
    }
    @PostMapping("/recent")
    public ResponseEntity<List<MyFile>> getRecentFiles(@RequestParam String userid) {
        List<MyFile> myFiles = fileService.getRecentFiles(userid);
        return ResponseEntity.ok(myFiles);
    }
    @PostMapping("/delete")
    public Result deleteFile(@RequestParam String token, @RequestParam String filename,@RequestParam String file_path){
        long userid = jwtTokenUtil.extractUserId(token);
        boolean result = fileService.fileToBin(userid, filename, file_path);
        if (result) {

            return Result.success("0", "success");
        }
        else {
            return Result.error("1", "移动文件到回收站失败");
        }

    }
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam long userid, @RequestParam String file_path) {
        try {
            // 获取用户名
            String username = userService.getUsernameById(userid);
            if (username == null) {
                return ResponseEntity.badRequest().body(null);
            }
            // 构造文件的完整路径
            String filePath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + file_path;
            // 创建一个UrlResource对象，它代表了要下载的文件
            Resource resource = new UrlResource(Paths.get(filePath).toUri());
            // 创建一个HTTP头信息对象，它告诉浏览器这是一个文件下载的响应
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
            // 返回一个ResponseEntity对象，它包含了要下载的文件的内容和HTTP头信息
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (Exception e) {
            // 如果出现任何异常，返回一个错误的响应
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/share")
    public Result shareFile(@RequestParam String token,
                            @RequestParam String filename,
                            @RequestParam String path,
                            @RequestParam String email,
                            @RequestParam int state) {
        // 解析token获取用户ID
        Long uid1 = jwtTokenUtil.extractUserId(token);
        if (uid1 == null) {
            return Result.error("1", "无效的token");
        }
        // 根据email查找用户ID
        Long uid2 = userService.getUserIdByEmail(email);
        if (uid2 == null) {
            return Result.error("2", "用户不存在");
        }
        // 将文件保存到uid2的uid2/share/路径下
        String destFolder = "D:/张伊扬/软件工程/data/"+ uid2 + "/share/";
        String destFilePath = destFolder + filename;
        String scPath = "D:/张伊扬/软件工程/data/"+ uid1 + "/" + path + "/" + filename;
        try {
            FileUtils.copyFile(new File(scPath), new File(destFilePath));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("3", "文件复制失败");
        }
        // 插入share表
        Share share = new Share();
        share.setUid1(uid1);
        share.setUid2(uid2);
        share.setFilename(filename);
        share.setPath(path);
        share.setState(state);
        boolean result = shareService.addShare(share);
        if (result) {
            return Result.success("0", "success");
        } else {
            return Result.error("4", "共享失败");
        }
    }
}