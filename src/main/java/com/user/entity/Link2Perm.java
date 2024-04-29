package com.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "link2perm")
public class Link2Perm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkid;

    @Column(nullable = false)
    private Integer permission;

    public Long getLinkid() {
        return linkid;
    }

    public void setLinkid(Long linkid) {
        this.linkid = linkid;
    }

    public Integer getPermission() {
        return permission;
    }

    public void setPermission(Integer permission) {
        this.permission = permission;
    }
}