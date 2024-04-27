package com.user.entity;

import org.apache.tomcat.jni.FileInfo;

import java.util.List;

public class FileResponse {

        private int errorCode;
        private List<File> data;

        // getters and setters

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public List<File> getData() {
        return data;
    }

    public void setData(List<File> data) {
        this.data = data;
    }
}
