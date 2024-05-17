package com.user.service;

import com.user.entity.Share;

import java.util.List;
import java.util.Map;

public interface ShareService {

    boolean addShare(Share share);

    List<Share> getSharesByUid2(Long uid2);
    Map<String, Object> getFileInfoById(long fileid);

    Share getShareFileInfoByFileid(long fileid);

    Map<Long, List<Share>> getSharesByUid2(long uid2);

    List<Share> getSharesByUid1(Long uid1);

    void saveShareFileInfo(Share share);

    void deleteShareFileInfo(Share share);

    List<Long> getUid2ListByFileid(long fileid);
}
