package com.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Table(name = "bin")
@Data
@Entity
public class Bin implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long binid;

    @Column(nullable = false)
    private long uid;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String path;


}
