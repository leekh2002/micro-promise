package com.gyuhyuk.price_alert.data.dao;

import com.gyuhyuk.price_alert.data.entity.UserEntity;

public interface UserDAO {
    UserEntity saveUser(UserEntity user);

    UserEntity getUserById(String id);

    void deleteUserById(String id);
}
