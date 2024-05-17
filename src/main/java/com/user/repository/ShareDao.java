package com.user.repository;

import com.user.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShareDao extends JpaRepository<Share,Long> {
    List<Share> findByUid2(long uid2);
    List<Share> findByUid1(long uid1);
    Share findByFileid(long fileid);
    @Query(value = "SELECT s.uid2 FROM Share s WHERE s.fileid=:fileid")
    List<Long> findUid2ListByFileid(@Param("fileid") long fileid);
}
