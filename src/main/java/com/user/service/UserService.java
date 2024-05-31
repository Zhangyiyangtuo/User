package com.user.service;

import com.user.entity.User;

public interface UserService {
    //update email
    boolean updateEmail(long uid, String email);

    User loginService(String email, String username, String password);

    long registService(String username, String password, String email);

    boolean modifyPassword(String email, String newPassword);


    User findByUid(long l);
    boolean updateUserInfo(long uid, String username);
    String getUsernameById(long userid);
    boolean changePassword(long userid, String newPassword);
    public boolean updateUserEmailAndName(long uid, String email, String name);

    Long getUserIdByEmail(String email);
    public int[] getSpaceUsage(long userid);
}