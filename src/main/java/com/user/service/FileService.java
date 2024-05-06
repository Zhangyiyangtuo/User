package com.user.service;

import com.user.entity.MyFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<MyFile> getAllFiles(Long userid, int sortord, int order);
    boolean addFile(long userid, String filename, String size, String fileUrl);
    public void downloadFile(String fileUrl, String username);
    public boolean moveFile(long userid, String prevPath, String newPath);
    boolean renameFile(long userid, String filePath, String newName);
    int countFiles(Long userid);
    public boolean updateFile(long userid, String filePath, MultipartFile file);
    List<MyFile> searchFiles(String userid, String filename);
    List<MyFile> getRecentFiles(String userid);
    public boolean deleteFile(long userid, String filePath);
}
