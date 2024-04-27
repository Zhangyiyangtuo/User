package com.user.repository;

import com.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User,Long> {
    User findByUsername(String username);

    User findByUsernameAndPassword(String username,String password);

    User findByEmail(String email);

    User findByUid(long uid);
}
