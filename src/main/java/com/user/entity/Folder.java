package com.user.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "folder")
public class Folder implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userid;

    @Column(nullable = false)
    private Long folderid;

    @Column(nullable = false)
    private String foldername;
    @Id
    @Column(nullable = false)
    private String fileid;

    @Column(nullable = false)
    private String fatherfolderid;

    @Column(nullable = false)
    private Date update_time;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private int mark;
}