package com.user.controller;

import com.user.entity.MyFile;
import com.user.service.UserService;
import com.user.service.serviceImpl.FileServiceImpl;
import com.user.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import  java.io.File;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileServiceImpl fileService;
    @Autowired
    private UserService userService;

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
    public Result updateFile(@RequestParam long userid, @RequestParam String file_path, @RequestParam MultipartFile file) {
        try {
            if (fileService.updateFile(userid, file_path, file)) {
                return Result.success(null, "success");
            } else {
                return Result.error("1", "更新文件失败");
            }
        } catch (Exception e) {
            return Result.error("1", "更新文件失败: " + e.getMessage());
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
    public Result deleteFile(@RequestParam long userid, @RequestParam String file_path){
        boolean result = fileService.deleteFile(userid, file_path);
        if (result) {
            return Result.success(null, "success");
        } else {
            return Result.error("1", "删除文件失败");
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
    @PostMapping("/folder/add")
    public Result addFolder(@RequestParam long userid, @RequestParam String path, @RequestParam String name) {
        try {
            // 获取用户名
            String username = userService.getUsernameById(userid);
            if (username == null) {
                return Result.error("1", "用户不存在");
            }
            // 构造文件夹的完整路径
            String folderPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + path + "/" + name;
            File folder = new File(folderPath);
            // 如果文件夹不存在，创建它
            if (!folder.exists()) {
                folder.mkdirs();
            }
            return Result.success(null, "succeed");
        } catch (Exception e) {
            return Result.error("1", "创建文件夹失败: " + e.getMessage());
        }
    }

    @PostMapping("folder/rename")
    public Result renameFolder(@RequestParam long userid, @RequestParam String path, @RequestParam String pre_name, @RequestParam String new_name) {
        try {
            // 获取用户名
            String username = userService.getUsernameById(userid);
            if (username == null) {
                return Result.error("1", "用户不存在");
            }
            // 构造旧文件夹和新文件夹的完整路径
            String oldFolderPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + path + "/" + pre_name;
            String newFolderPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + path + "/" + new_name;
            File oldFolder = new File(oldFolderPath);
            File newFolder = new File(newFolderPath);
            // 如果旧文件夹存在，且新文件夹不存在，重命名它
            if (oldFolder.exists() && !newFolder.exists()) {
                oldFolder.renameTo(newFolder);
            }
            return Result.success(null, "succeed");
        } catch (Exception e) {
            return Result.error("1", "重命名文件夹失败: " + e.getMessage());
        }
    }
    @PostMapping("folder/move")
    public Result moveFolder(@RequestParam long userid, @RequestParam String pre_path, @RequestParam String new_path, @RequestParam String name) {
        try {
            // 获取用户名
            String username = userService.getUsernameById(userid);
            if (username == null) {
                return Result.error("1", "用户不存在");
            }
            // 构造旧文件夹和新文件夹的完整路径
            String oldFolderPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + pre_path + "/" + name;
            String newFolderPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + new_path + "/" + name;
            File oldFolder = new File(oldFolderPath);
            File newFolder = new File(newFolderPath);
            // 如果旧文件夹存在，且新文件夹不存在，移动它
            if (oldFolder.exists() && !newFolder.exists()) {
                oldFolder.renameTo(newFolder);
            }
            return Result.success(null, "succeed");
        } catch (Exception e) {
            return Result.error("1", "移动文件夹失败: " + e.getMessage());
        }
    }
}