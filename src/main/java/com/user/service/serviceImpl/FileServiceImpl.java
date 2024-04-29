package com.user.service.serviceImpl;

import com.user.entity.File;
import com.user.service.FileService;
import com.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private UserService userService;

    // In FileServiceImpl.java
    @Override
    public List<File> getAllFiles(Long userid, int sortord, int order) {
        String basePath;
        if (userid != null) {
            String username = userService.getUsernameById(userid);
            if (username == null) {
                throw new IllegalArgumentException("Invalid userid");
            }
            basePath = System.getProperty("user.home") + "/Desktop/test/" + username;
        } else {
            basePath = System.getProperty("user.home") + "/Desktop/test/";
        }

        List<File> files;
        try (Stream<Path> paths = Files.walk(Paths.get(basePath))) {
            files = paths
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        // create a File object from the Path
                        File file = new File();
                        file.setFilename(path.getFileName().toString());
                        // Get the parent directory name
                        String updater = getUpdater(path);
                        // Set updater to the top level directory name under ~/Desktop/test/
                        file.setUpdater(updater);
                        try {
                            file.setUpdateTime(String.valueOf(Files.getLastModifiedTime(path).toInstant()));
                            file.setSize(Files.size(path) + " bytes");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return file;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading files", e);
        }

        Comparator<File> comparator = switch (sortord) {
            case 1 -> Comparator.comparing(File::getUpdateTime);
            case 2 -> Comparator.comparing(File::getUpdateTime);
            case 3 -> Comparator.comparing(File::getSize);
            case 4 -> Comparator.comparing(File::getFilename);
            default -> throw new IllegalArgumentException("Invalid sortord value");
        };
        if (order == 2) {
            comparator = comparator.reversed();
        }
        files.sort(comparator);

        return files;
    }

    private String getUpdater(Path path) {
        Path parentPath = path.getParent();
        if (parentPath != null && parentPath.getFileName().toString().equals("test")) {
            return "root";
        } else {
            Path ChildrenPath = null;
            while (parentPath != null && !parentPath.getFileName().toString().equals("test")) {
                ChildrenPath = parentPath;
                parentPath = parentPath.getParent();
            }
            //获取子文件夹名称
            return ChildrenPath.getFileName().toString();
        }
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

    @Override
    public boolean moveFile(long userid, String prevPath, String newPath) {
        String username = userService.getUsernameById(userid);
        if (username == null) {
            return false;
        }

        String fullPrevPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + prevPath;
        Path oldPath = Paths.get(fullPrevPath);
        if (!Files.exists(oldPath)) {
            return false;
        }

        // Extract the file name from the old path
        String fileName = oldPath.getFileName().toString();

        // Append the file name to the new path
        String fullNewPath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + newPath + "/" + fileName;

        // Create the new directory if it does not exist
        Path newDirPath = Paths.get(System.getProperty("user.home") + "/Desktop/test/" + username + "/" + newPath);
        if (!Files.exists(newDirPath)) {
            try {
                Files.createDirectories(newDirPath);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            Files.move(oldPath, Paths.get(fullNewPath));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean renameFile(long userid, String filePath, String newName) {
        String username = userService.getUsernameById(userid);
        if (username == null) {
            return false;
        }

        String fullFilePath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + filePath;
        Path oldPath = Paths.get(fullFilePath);
        if (!Files.exists(oldPath)) {
            return false;
        }

        // Extract the directory path from the old path
        Path dirPath = oldPath.getParent();

        // Create the new path with the new name
        Path newPath = Paths.get(dirPath.toString(), newName);

        try {
            Files.move(oldPath, newPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public int countFiles(Long userid) {
        String basePath;
        if (userid != null) {
            String username = userService.getUsernameById(userid);
            if (username == null) {
                return -1;
            }
            basePath = System.getProperty("user.home") + "/Desktop/test/" + username;
        } else {
            basePath = System.getProperty("user.home") + "/Desktop/test/";
        }

        try (Stream<Path> files = Files.walk(Paths.get(basePath))) {
            return (int) files.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public void downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String[] pathParts = url.getPath().split("/");
            String fileName = pathParts[pathParts.length - 1];

            Path localPath = Paths.get(System.getProperty("user.home") + "/Desktop/test/tmp/" + fileName);
            try (InputStream in = url.openStream();
                 ReadableByteChannel rbc = Channels.newChannel(in)) {
                Files.write(localPath, in.readAllBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean updateFile(long userid, String filePath, String fileUrl) {
        String username = userService.getUsernameById(userid);
        if (username == null) {
            return false;
        }

        String fullFilePath = System.getProperty("user.home") + "/Desktop/test/" + username + "/" + filePath;
        Path oldPath = Paths.get(fullFilePath);
        if (!Files.exists(oldPath)) {
            return false;
        }

        // Extract the file name from the file path
        String[] pathParts = fileUrl.split("/");
        String fileName = pathParts[pathParts.length - 1];

        // Download the new file to a temporary location
        downloadFile(fileUrl);

        // Replace the old file with the new file
        Path tempPath = Paths.get(System.getProperty("user.home") + "/Desktop/test/tmp/" + fileName);
        try {
            Files.move(tempPath, oldPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    // In FileServiceImpl.java
    @Override
    public List<File> searchFiles(String userid, String filename) {
        String username = userService.getUsernameById(Long.parseLong(userid));
        if (username == null) {
            throw new IllegalArgumentException("Invalid userid");
        }

        String basePath = System.getProperty("user.home") + "/Desktop/test/" + username;
        List<File> files;
        try (Stream<Path> paths = Files.walk(Paths.get(basePath))) {
            files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains(filename))
                    .map(path -> {
                        // create a File object from the Path
                        File file = new File();
                        file.setFilename(path.getFileName().toString());
                        file.setUpdater(username);
                        try {
                            file.setUpdateTime(String.valueOf(Files.getLastModifiedTime(path).toInstant()));
                            file.setSize(Files.size(path) + " bytes");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return file;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading files", e);
        }
        return files;
    }
    @Override
    public List<File> getRecentFiles(String userid) {
        String username = userService.getUsernameById(Long.parseLong(userid));
        if (username == null) {
            throw new IllegalArgumentException("Invalid userid");
        }

        String basePath = System.getProperty("user.home") + "/Desktop/test/" + username;
        List<File> files;
        try (Stream<Path> paths = Files.walk(Paths.get(basePath))) {
            files = paths
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        // create a File object from the Path
                        File file = new File();
                        file.setFilename(path.getFileName().toString());
                        file.setUpdater(username);
                        try {
                            file.setUpdateTime(String.valueOf(Files.getLastModifiedTime(path).toInstant()));
                            file.setSize(Files.size(path) + " bytes");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return file;
                    })
                    .filter(file -> {
                        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
                        return LocalDateTime.parse(file.getUpdateTime()).isAfter(oneMonthAgo);
                    })
                    .sorted(Comparator.comparing(File::getUpdateTime).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading files", e);
        }
        return files;
    }
}
