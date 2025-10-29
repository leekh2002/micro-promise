package com.gyuhyuk.micro_promise.data.dao;

import com.gyuhyuk.micro_promise.data.entity.UserEntity;

public interface UserDAO {
    UserEntity saveUser(UserEntity user);

    UserEntity getUserById(String id);

    void deleteUserById(String id);
}
