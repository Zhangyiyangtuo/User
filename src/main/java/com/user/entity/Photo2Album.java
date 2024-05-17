package com.user.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "photo2album")
@IdClass(Photo2AlbumId.class)
public class Photo2Album {
    @Id
    @Column(name = "uid")
    private long uid;

    @Id
    @Column(name = "photoname")
    private String photoname;

    @Column(name = "albumid")
    private int albumid;

    public void setUid(long userid) {
        this.uid = userid;
    }

    public void setPhotoname(String photoName) {
        this.photoname = photoName;
    }

    public void setAlbumid(long albumId) {
        this.albumid = (int) albumId;
    }

    public String getPhotoname() {
        return photoname;
    }

    public String getPhoto() {
        return photoname;
    }

    public String getPath() {
        return photoname;
    }

    public String getAlbumid() {
        return String.valueOf(albumid);
    }

    // getters and setters
}