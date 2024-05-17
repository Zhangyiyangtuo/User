package com.user.controller;

import com.user.entity.Bin;
import com.user.entity.Share;
import com.user.entity.User;
import com.user.repository.UserDao;
import com.user.service.ShareService;
import com.user.utils.JwtTokenUtil;
import com.user.utils.Result;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.MimeTypeUtils;



@RestController
@RequestMapping("/api/user/shared")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDao userDao;


    @GetMapping("/otherget")
    public ResponseEntity<Object> getShareFiles(@RequestParam String token) {
        //解析token获取uid2
        Long uid2 = jwtTokenUtil.extractUserId(token);
        if (uid2 == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //根据uid2查找对应的所有分享文件
        List<Share> shares = shareService.getSharesByUid2(uid2);
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            for (Share share: shares) {
                //根据path构造出文件对象并获取文件流转化为字节数组
                String path = "D:/张伊扬/软件工程/data/"+ uid2 + "/share/"  + share.getFilename();
                Resource resource = new UrlResource(Paths.get(path).toUri());
                InputStream inputStream = resource.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }

                long uid1 = share.getUid1();
                User user = userDao.findByUid(uid1);

                String mimeType = Files.probeContentType(Paths.get(path));
                String fileTypeText = getFileTypeText(mimeType);

                //将获取的文件信息封装到Map中并添加到resultLi列表中返回给前端
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("fileid", share.getFileid());
                resultMap.put("filename", share.getFilename());
                resultMap.put("size", resource.contentLength());
                resultMap.put("type", Files.probeContentType(Paths.get(path)));
                resultMap.put("state", share.getState());
                resultMap.put("fileContent", byteArrayOutputStream.toByteArray());
                resultMap.put("username", user.getUsername());
                resultMap.put("email", user.getEmail());
                resultMap.put("avatarId", user.getAvatarID());
                resultList.add(resultMap);
            }
            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    private String getFileTypeText(String mimeType) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("application/msword", "DOC 文档");
        typeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "DOCX 文档");
        typeMap.put("application/vnd.ms-excel", "Excel 文档");
        typeMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel 文档");
        typeMap.put("application/pdf", "PDF 文件");

        return typeMap.getOrDefault(mimeType, "未知类型");
    }


    @GetMapping("/ownget")
    public ResponseEntity<Object> getShareFiles1(@RequestParam String token) {
        //解析token获取uid1
        Long uid1 = jwtTokenUtil.extractUserId(token);
        if (uid1 == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //根据uid1查找对应的所有分享文件
        List<Share> shares = shareService.getSharesByUid1(uid1);
        Map<String, List<Map<String, Object>>> fileMap = new HashMap<>(); //用于将同一 filename 的文件信息做分类

        try {
            Map<String, byte[]> fileContentMap = new HashMap<>(); //用于存储已加载的文件内容
            for (Share share: shares) {
                String path = "D:/张伊扬/软件工程/data/"+ uid1 + "/"  + share.getPath() + "/" + share.getFilename();
                Resource resource = new UrlResource(Paths.get(path).toUri());
                InputStream inputStream = resource.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }

                long uid2 = share.getUid2();
                User user = userDao.findByUid(uid2);

                FileShareInfo fileShareInfo = new FileShareInfo();
                fileShareInfo.setFileid(share.getFileid());
                fileShareInfo.setUsername(user.getUsername());



                // 将文件信息封装为一个Map对象
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileShareInfo", fileShareInfo);
                fileInfo.put("filename", share.getFilename());
                fileInfo.put("path", share.getPath());
                fileInfo.put("size", byteArrayOutputStream.toByteArray().length);
                fileInfo.put("type", Files.probeContentType(Paths.get(path)));
                fileInfo.put("state", share.getState());
                fileInfo.put("fileContent", byteArrayOutputStream.toByteArray());

                // 将同一 filename 的文件信息保存在同一个列表中，添加到 fileMap 中
                String filename = share.getFilename();
                List<Map<String, Object>> fileList = fileMap.computeIfAbsent(filename, k -> new ArrayList<>());
                fileList.add(fileInfo);
            }

            // 将同一 filename 的文件信息封装到一个对象中，并添加到返回结果的 List 中
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : fileMap.entrySet()) {
                String filename = entry.getKey();
                List<Map<String, Object>> fileList = entry.getValue();

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileShareInfolist", fileList.stream().map(f -> f.get("fileShareInfo")).collect(Collectors.toList()));
                fileInfo.put("filename", filename);
                fileInfo.put("path", fileList.get(0).get("path"));
                fileInfo.put("size", fileList.get(0).get("size"));
                fileInfo.put("type", fileList.get(0).get("type"));
                fileInfo.put("state", fileList.get(0).get("state"));
                // 对于多个文件，将第一个文件的 fileContent 作为代表返回
                fileInfo.put("fileContent", fileList.get(0).get("fileContent"));
                //fileInfo.put("sharerlist", fileList.stream().map(f -> f.get("sharer")).collect(Collectors.toList()));
                resultList.add(fileInfo);
            }

            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }

    public class FileShareInfo {
        @Setter
        @Getter
        private long fileid;
        @Setter
        @Getter
        private String username;


    }

    @GetMapping("/check")
    public ResponseEntity<Object> checkShare(@RequestParam String token,@RequestParam long fileid) {
        // 解析token获取uid2，校验fileid是否为当前用户分享的文件
        Long uid2 = jwtTokenUtil.extractUserId(token);
        Share share = shareService.getShareFileInfoByFileid(fileid);
        if (share == null || share.getUid2() != uid2) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 获取文件信息和用户信息，并返回给前端
        Map<String, Object> resultMap = shareService.getFileInfoById(fileid);
        if (resultMap == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resultMap);
    }

    @GetMapping("/allcheck")
    public ResponseEntity<Object> checkAllShare(@RequestParam String token) {
        Long uid2 = jwtTokenUtil.extractUserId(token);
        if (uid2 == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<Long, List<Share>> sharesMap = getSharesData(uid2);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map.Entry<Long, List<Share>> entry : sharesMap.entrySet()) {
            Long uid1 = entry.getKey();
            List<Share> shares = entry.getValue();

            User user = userDao.findByUid(uid1);
            String username = user.getUsername();
            int avatarId = user.getAvatarID();
            String email = user.getEmail();

            List<Map<String, Object>> fileList = new ArrayList<>();
            for (Share share : shares) {
                try {
                    Map<String, Object> fileMap = getShareFileData(share);
                    fileList.add(fileMap);
                } catch (IOException e) {
                    // 处理异常
                }
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("uid1", uid1);
            resultMap.put("username", username);
            resultMap.put("email", email);
            resultMap.put("avatarId", avatarId);
            resultMap.put("filelist", fileList);
            resultList.add(resultMap);
        }
        return ResponseEntity.ok(resultList);
    }

    private Map<Long, List<Share>> getSharesData(Long uid2) {
        List<Share> sharesList = shareService.getSharesByUid2(uid2);
        Map<Long, List<Share>> sharesMap = new HashMap<>();
        for (Share share : sharesList) {
            Long uid1 = share.getUid1();
            if (sharesMap.containsKey(uid1)) {
                sharesMap.get(uid1).add(share);
            } else {
                List<Share> shares = new ArrayList<>();
                shares.add(share);
                sharesMap.put(uid1, shares);
            }
        }
        return sharesMap;
    }

    private Map<String, Object> getShareFileData(Share share) throws IOException {
        String path = "D:/张伊扬/软件工程/data/"+ share.getUid1() + "/" + share.getPath() + "/" + share.getFilename();
        Resource resource = new UrlResource(Paths.get(path).toUri());
        InputStream inputStream = resource.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        byte[] fileData = byteArrayOutputStream.toByteArray();

        Map<String, Object> fileMap = new HashMap<>();
        fileMap.put("fileid", share.getFileid());
        fileMap.put("filename", share.getFilename());
        fileMap.put("size", resource.contentLength());
        fileMap.put("filedata", fileData);
        return fileMap;
    }

    @PutMapping("/modify")
    public ResponseEntity<Object> modifyShare(@RequestParam long fileid, @RequestParam int state) {
        Share share = shareService.getShareFileInfoByFileid(fileid);
        if (share == null) {
            return ResponseEntity.notFound().build();
        }
        share.setState(state);
        shareService.saveShareFileInfo(share);
        return ResponseEntity.ok(Result.success("更新状态成功"));
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Object> cancelShare(@RequestParam long fileid) throws IOException {
        Share share = shareService.getShareFileInfoByFileid(fileid);
        if (share == null) {
            return ResponseEntity.notFound().build();
        }
        String filepath = "D:/张伊扬/软件工程/data/"+ share.getUid2() + "/share/"  +  share.getFilename();
        Files.deleteIfExists(Paths.get(filepath));
        shareService.deleteShareFileInfo(share);
        return ResponseEntity.ok(Result.success("取消分享成功"));
    }
}


