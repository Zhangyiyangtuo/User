package com.user.repository;

import com.user.entity.Photo2AlbumId;
import org.springframework.data.jpa.repository.JpaRepository;
import com.user.entity.Photo2Album;

public interface Photo2AlbumDao extends JpaRepository<Photo2Album, Photo2AlbumId> {

}