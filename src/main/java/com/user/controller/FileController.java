package com.user.controller;

import com.user.entity.MyFile;
import com.user.service.UserService;
import com.user.service.serviceImpl.FileServiceImpl;
import com.user.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public Result deleteFile(@RequestParam long userid, @RequestParam String file_path){
        boolean result = fileService.deleteFile(userid, file_path);
        if (result) {
            return Result.success(null, "success");
        } else {
            return Result.error("1", "删除文件失败");
        }
    }
}