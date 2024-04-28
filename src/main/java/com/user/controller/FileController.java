package com.user.controller;

import com.user.entity.FileRequest;
import com.user.entity.FileResponse;
import com.user.service.serviceImpl.FileServiceImpl;
import com.user.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}