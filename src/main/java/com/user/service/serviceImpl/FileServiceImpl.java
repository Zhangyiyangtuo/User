package com.user.service.serviceImpl;

import com.user.entity.File;
import com.user.entity.FileRequest;
import com.user.service.FileService;
import com.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private UserService userService;

    public List<File> getFileInfos(FileRequest request) {
        List<File> files = new ArrayList<>();
        try {
//
            java.io.File outputFile = new java.io.File(System.getProperty("user.home") + "/Desktop/test/file.txt");

            // 执行 ls 命令
            Process process = Runtime.getRuntime().exec("ls -la " + request.getPath());

            // 获取命令的输出流
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // 将输出流写入到文件中
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile.getAbsolutePath()));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            // 关闭流
            writer.close();
            reader.close();

            // 等待命令执行完成
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                // 命令执行成功，读取文件并创建File对象
                BufferedReader fileReader = new BufferedReader(new FileReader(outputFile));
                String fileLine;
                while ((fileLine = fileReader.readLine()) != null) {
                    String[] parts = fileLine.split("\\s+");
                    if (parts.length >= 9) {
                        File file = new File();
                        file.setFilename(parts[8]);
                        file.setSize(parts[4]);
                        file.setUpdateTime(parts[5] + " " + parts[6] + " " + parts[7]);
                        file.setUpdater(parts[2]);
                        files.add(file);
                    }
                }
                fileReader.close();
            } else {
                // 命令执行失败
                System.out.println("命令执行失败");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return files;
    }

    @Override
    public boolean addFile(long userid, String filename, String size, String fileUrl) {
        String username = userService.getUsernameById(userid);
        if (username != null) {
            // Ensure that fileUrl includes a protocol
            if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
                fileUrl = "http://" + fileUrl;
            }
            downloadFile(fileUrl, username);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void downloadFile(String fileUrl, String username) {
        try {
            URL url = new URL(fileUrl);
            String[] pathParts = url.getPath().split("/");

            Path localPath = Paths.get(System.getProperty("user.home") + "/Desktop/test/" + username);
            for (int i = 0; i < pathParts.length - 1; i++) {
                localPath = localPath.resolve(pathParts[i]);
                if (!Files.exists(localPath)) {
                    Files.createDirectory(localPath);
                }
            }

            localPath = localPath.resolve(pathParts[pathParts.length - 1]);
            try (InputStream in = url.openStream();
                 ReadableByteChannel rbc = Channels.newChannel(in)) {
                Files.write(localPath, in.readAllBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
