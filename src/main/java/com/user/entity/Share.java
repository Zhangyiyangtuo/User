package com.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Table(name = "share")
@Data
@Entity
public class Share implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long fileid;

    @Column(nullable = false)
    private long uid1;

    @Column(nullable = false)
    private long uid2;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int state;

}

