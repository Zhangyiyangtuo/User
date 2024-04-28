package com.user.service;

import com.user.entity.File;
import com.user.entity.FileRequest;

import java.util.List;

public interface FileService {
    public List<File> getFileInfos(FileRequest request);
    boolean addFile(long userid, String filename, String size, String fileUrl);
    public void downloadFile(String fileUrl, String username);
    public boolean moveFile(long userid, String prevPath, String newPath);
    boolean renameFile(long userid, String filePath, String newName);
    int countFiles(Long userid);
}
