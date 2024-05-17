package com.user.service.serviceImpl;

import com.user.entity.Bin;
import com.user.entity.User;
import com.user.repository.BinDao;
import com.user.repository.UserDao;
import com.user.service.BinService;
import com.user.service.UserService;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;


@Service
public class BinServiceImpl implements BinService {
    @Resource
    private BinDao binDao;

    @Override
    public String getSrcPath(String fileName, long uid) {
        Bin bin=binDao.findByUidAndFilename(uid,fileName);
        return bin.getPath();
    }

    @Override
    public Bin getBin(String fileName, long uid) {
        Bin bin=binDao.findByUidAndFilename(uid,fileName);
        return bin;
    }

    @Override
    public void deleteByFilenameAndUid(String filename, long uid) {
        binDao.deleteByFilenameAndUid(filename,uid);

    }

    @Override
    public void save(Bin bin) {
        binDao.save(bin);

    }
}
