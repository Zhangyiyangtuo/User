package com.user.controller;

import com.user.entity.FileRequest;
import com.user.entity.FileResponse;
import com.user.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/getAll")
    public ResponseEntity<Object> getFileInfo(@RequestBody FileRequest request) {
        FileResponse response = new FileResponse();
        response.setErrorCode(0);
        response.setData(fileService.getFileInfos(request));
        return ResponseEntity.ok(response);
    }
}