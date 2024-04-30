package com.user.service.serviceImpl;

import com.user.entity.User;
import com.user.repository.UserDao;
import com.user.service.UserService;
import jakarta.annotation.Resource;
import com.user.repository.Photo2AlbumDao;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;
    @Override
    public User loginService(String email,String username, String password) {
        User user = userDao.findByUsernameAndPassword(username,password);
        if(user!=null){
            user.setPassword("");
            return user;
        }
        else{
            return null;
        }

    }

    @Override
    public long registService(String username, String password, String email) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            return -1;
        } else if (userDao.findByUsername(username) != null) {
            return -2;
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setEmail(email);
            newUser.setGroupid(1);
            newUser.setUserrank(1);
            newUser.setSpace_usage_doc(0);
            newUser.setSpace_usage_photo(0);
            newUser.setAvatarID(0);

            userDao.save(newUser);
            try {
                String[] cmd = { "/bin/sh", "-c", "mkdir -p ~/Desktop/test/" + username };
                Process proc = Runtime.getRuntime().exec(cmd);
                proc.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        }
    }


    @Override
    public boolean modifyPassword(String email, String newPassword) {
        User user=userDao.findByEmail(email);
        if(user!=null)
        {
            user.setPassword(newPassword);
            userDao.save(user);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public User findByUid(long uid) {
        return userDao.findByUid(uid);
    }
    @Override
    public boolean updateUserInfo(long uid, String username) {
        User user = userDao.findByUid(uid);
        if (user != null) {
            user.setUsername(username);
            userDao.save(user);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getUsernameById(long userid) {
        Optional<User> user = userDao.findById(userid);
        return user.map(User::getUsername).orElse(null);
    }
    @Override
    public boolean changePassword(long userid, String newPassword) {
        User user = userDao.findByUid(userid);
        if (user != null) {
            user.setPassword(newPassword);
            userDao.save(user);
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean updateUserEmailAndName(long uid, String email, String name) {
        User user = userDao.findByUid(uid);
        if (user != null) {
            user.setEmail(email);
            user.setUsername(name);
            userDao.save(user);
            return true;
        } else {
            return false;
        }
    }
}
