package com.user.service.serviceImpl;

import com.user.entity.User;
import com.user.repository.UserDao;
import com.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
}
