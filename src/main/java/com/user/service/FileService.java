package com.user.service;

import com.user.entity.MyFile;

import java.util.List;

public interface FileService {
    List<MyFile> getAllFiles(Long userid, int sortord, int order);
    boolean addFile(long userid, String filename, String size, String fileUrl);
    public void downloadFile(String fileUrl, String username);
    public boolean moveFile(long userid, String prevPath, String newPath);
    boolean renameFile(long userid, String filePath, String newName);
    int countFiles(Long userid);
    boolean updateFile(long userid, String filePath, String fileUrl);
    List<MyFile> searchFiles(String userid, String filename);
    List<MyFile> getRecentFiles(String userid);
    public boolean deleteFile(long userid, String filePath);
}
