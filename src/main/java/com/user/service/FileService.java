package com.user.service;

import com.user.entity.File;
import com.user.entity.FileRequest;

import java.util.List;

public interface FileService {
    public List<File> getFileInfos(FileRequest request);
    boolean addFile(long userid, String filename, String size, String fileUrl);
    public void downloadFile(String fileUrl, String username);
}
