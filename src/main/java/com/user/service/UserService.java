package com.user.service;

import com.user.entity.User;

public interface UserService {

    User loginService(String email,String username, String password);

    long registService(String username, String password,String email);

    boolean modifyPassword(String email, String newPassword);


}
