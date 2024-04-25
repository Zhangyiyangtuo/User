package com.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Table(name = "user")
@Data
@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uid;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, length = 20)
    private String password;

    @Column(nullable = false, length = 40)
    private String email;

    @Column(nullable = false)
    private int groupid;

    @Column(nullable = false)
    private int userrank;

    @Column(nullable = false)
    private int space_usage_photo;

    @Column(nullable = false)
    private int space_usage_doc;

    @Column(nullable = false)
    private int avatarID;

    /*
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date regdate;
    */

}
