package com.user.service.serviceImpl;

import com.user.entity.Share;
import com.user.entity.User;
import com.user.repository.ShareDao;
import com.user.repository.UserDao;
import com.user.service.ShareService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShareServiceImpl implements ShareService {

    @Resource
    private ShareDao shareDao;
    @Resource
    private UserDao userDao;
    @Override
    public boolean addShare(Share share) {
        try {
            shareDao.save(share);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Share> getSharesByUid2(Long uid2) {
        return shareDao.findByUid2(uid2);
    }
    @Override
    public Map<String, Object> getFileInfoById(long fileid) {
        Share share = shareDao.findByFileid(fileid);
        if (share == null) {
            return null;
        }
        //根据uid1在user表中查找对应的用户信息
        User user = userDao.findByUid(share.getUid1());
        String username = user.getUsername();
        int avatarId = user.getAvatarID();
        String email = user.getEmail();
        //将文件信息和用户信息组合成一个Map返回给前端
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("username", username);
        resultMap.put("avatarId", avatarId);
        resultMap.put("email", email);
        return resultMap;
    }

    @Override
    public Share getShareFileInfoByFileid(long fileid) {
        return shareDao.findByFileid(fileid);

    }

    @Override
    public Map<Long, List<Share>> getSharesByUid2(long uid2) {
        List<Share> shares = shareDao.findByUid2(uid2);
        Map<Long, List<Share>> resultMap = new HashMap<>();
        for (Share share : shares) {
            long uid1 = share.getUid1();
            List<Share> list = resultMap.getOrDefault(uid1, new ArrayList<>());
            list.add(share);
            resultMap.put(uid1, list);
        }
        return resultMap;
    }

    @Override
    public List<Share> getSharesByUid1(Long uid1) {
        return shareDao.findByUid1(uid1);
    }

    @Override
    public void saveShareFileInfo(Share share) {
        shareDao.save(share);
    }

    @Override
    public void deleteShareFileInfo(Share share) {
        shareDao.delete(share);
    }

    @Override
    public List<Long> getUid2ListByFileid(long fileid) {
        return shareDao.findUid2ListByFileid(fileid);

    }


}
