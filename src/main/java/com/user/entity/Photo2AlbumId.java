package com.user.entity;

import java.io.Serializable;
import java.util.Objects;

public class Photo2AlbumId implements Serializable {
    private long uid;
    private String photoname;

    // getters and setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo2AlbumId that = (Photo2AlbumId) o;
        return uid == that.uid && Objects.equals(photoname, that.photoname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, photoname);
    }
}