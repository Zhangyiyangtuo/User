package com.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Table(name = "file")
@Data
@Entity
public class File implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long fileid;

    @Column(nullable = false,  length = 20)
    private String filename;

    @Column(length = 20)
    private long foldid;

    @Column(length = 20)
    private String foldname;

    @Column(length = 20)
    private String  filelable;

    @Column(nullable = false,length = 20)
    private String  type;

    @Column(nullable = false,length = 20)
    private String  size;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date update_time;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delete_time;

    @Column(nullable = false,length = 20)
    private String  link;

}
