package com.user.entity;

public class FileRequest {
    private Integer userId;
    private int sortord;
    private int order;
    public String getPath() {
        return "~/Desktop";
    }
    public int getSortord() {
        return sortord;
    }

    public void setSortord(int sortord) {
        this.sortord = sortord;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // getters and setters
}
