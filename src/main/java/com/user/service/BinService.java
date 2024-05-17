package com.user.service;
import com.user.entity.Bin;
import com.user.repository.BinDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
public interface BinService {


    String getSrcPath(String fileName, long uid) ;
    Bin getBin(String fileName,long uid);

    void deleteByFilenameAndUid(String filename, long uid);

    void save(Bin bin);

}
