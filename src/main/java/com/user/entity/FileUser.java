package com.user.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "file_user")
public class FileUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userid;

    @Column(nullable = false)
    private Long fileid;

    @Column(nullable = false)
    private int isShared;

    @Column(nullable = false)
    private int state;

    @Column(nullable = false)
    private int isdeleted;
}