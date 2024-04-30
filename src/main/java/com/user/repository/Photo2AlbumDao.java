package com.user.repository;

import com.user.entity.Photo2AlbumId;
import org.springframework.data.jpa.repository.JpaRepository;
import com.user.entity.Photo2Album;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
@Repository
@Service
public interface Photo2AlbumDao extends JpaRepository<Photo2Album, Photo2AlbumId> {

    List<Photo2Album> findByAlbumid(long albumId);
    Photo2Album findByUidAndPhotoname(long uid, String photoname);
}