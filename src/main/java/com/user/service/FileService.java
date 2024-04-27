
package com.user.service;

import com.user.entity.File;
import com.user.entity.FileRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

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
}