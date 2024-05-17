package com.user.controller;

import com.user.entity.Bin;
import com.user.service.BinService;
import com.user.utils.JwtTokenUtil;
import com.user.utils.Result;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user/cancel")
public class BinController {
    private final String binPath = "/bin"; // 回收站文件夹路径
    private final String binPath1 = "D:/张伊扬/软件工程/data/";
    @Resource
    private BinService binService;


    private final JwtTokenUtil jwtTokenUtil; // 添加 JwtTokenUtil 类的引用
    public BinController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }
    @GetMapping("/get")
    public List<FileInfo> getBinFiles(@RequestParam String token) throws IOException {

        long uid = jwtTokenUtil.extractUserId(token);
        String binFolder =  binPath1 + String.valueOf(uid)+ binPath;

        // 遍历回收站文件夹中的所有文件，将需要的文件信息转换为FileInfo类并添加到列表中
        List<FileInfo> fileList = new ArrayList<>();
        Files.walk(Paths.get(binFolder)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, len);
                    }
                    String fileName = filePath.toFile().getName();
                    String srcPath = binService.getSrcPath(fileName, uid); // 从数据库中获取原始文件路径
                    String contentType = Files.probeContentType(filePath);
                    long fileSize = Files.size(filePath);
                    LocalDateTime deleteTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(filePath).toInstant(),
                            ZoneId.systemDefault());
                    //FileInputStream fis = new FileInputStream(filePath.toFile());

                    FileInfo fileInfo = new FileInfo(fileName, srcPath, contentType, fileSize, deleteTime);
                    fileList.add(fileInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return fileList;
    }
    @DeleteMapping("delete")
    public Result deleteBinFile(@RequestParam String filename, @RequestParam String token,@RequestParam String path) throws IOException {
        long uid = jwtTokenUtil.extractUserId(token);
        Bin bin = binService.getBin(filename, uid);
        if (bin != null&& bin.getPath().equals(path)) { // 如果找到了对应的 Bin 实体，则删除文件和数据库记录
            String filePath =  binPath1+String.valueOf(bin.getUid()) + binPath +"/"+ filename;
            Files.deleteIfExists(Paths.get(filePath));
            binService.deleteByFilenameAndUid(filename, uid);
            return Result.success("success");
        } else {
            return Result.error("1", "删除失败！");
        }
      }
    @PostMapping("/redo")
    public Result recoveryBinFile(@RequestParam String filename, @RequestParam String path, @RequestParam String token) throws IOException {
        long uid = jwtTokenUtil.extractUserId(token);

        Bin bin = binService.getBin(filename, uid);
        if (bin != null && bin.getPath().equals(path)) { // 如果找到了对应的 Bin 实体并且文件路径相同，则进行恢复操作
            String binFilePath = binPath1 + String.valueOf(uid) + binPath +"/"+  filename;
            String desFilePath = binPath1 + String.valueOf(uid) + "/" + path +"/"+filename;

            if (Files.exists(Paths.get(binFilePath))) { // 如果回收站中的文件存在，则将原始文件移动到回收站中的位置
                Files.move(Paths.get(binFilePath), Paths.get(desFilePath), StandardCopyOption.REPLACE_EXISTING);
            } else { // 如果回收站中的文件不存在，则将原始文件复制到回收站中的位置
                Files.copy(Paths.get(binFilePath), Paths.get(desFilePath), StandardCopyOption.REPLACE_EXISTING);
            }

            binService.deleteByFilenameAndUid(filename, uid); // 从回收站数据库中删除该文件

            return Result.success("success");
        } else { // 如果找不到对应的 Bin 实体或者文件路径不相同，则返回错误信息
            return Result.error("1", "文件不存在或路径不正确");
        }
    }


    @Data
    @AllArgsConstructor
    public class FileInfo {
        private String fileName;
        private String srcPath;
        private String contentType;
        private long fileSize;
        private LocalDateTime deleteTime;
        //private byte[] buffer;
    }
}
