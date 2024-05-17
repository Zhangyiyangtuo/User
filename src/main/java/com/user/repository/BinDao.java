package com.user.repository;

import com.user.entity.Bin;
import com.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface BinDao extends JpaRepository<Bin,Long> {

    Bin findByUidAndFilename(long uid,String filename);
    @Transactional
    @Modifying
    void deleteByFilenameAndUid(String filename, long uid);







}
