package com.user.controller;

import com.user.entity.FileRequest;
import com.user.entity.FileResponse;
import com.user.service.serviceImpl.FileServiceImpl;
import com.user.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileServiceImpl fileService;

    @PostMapping("/getAll")
    public ResponseEntity<Object> getFileInfo(@RequestBody FileRequest request) {
        FileResponse response = new FileResponse();
        response.setErrorCode(0);
        response.setData(fileService.getFileInfos(request));
        return ResponseEntity.ok(response);
    }
    @PostMapping("/add")
    public Result addFile(@RequestParam long userid, @RequestParam String filename, @RequestParam String size, @RequestParam String fileUrl) {
        boolean result = fileService.addFile(userid, filename, size, fileUrl);
        if (result) {
            return Result.success(null, "success");
        } else {
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
}