package com.gyuhyuk.price_alert.data.dao.impl;

import com.gyuhyuk.price_alert.data.dao.UserDAO;
import com.gyuhyuk.price_alert.data.entity.UserEntity;
import com.gyuhyuk.price_alert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDAOImpl implements UserDAO {
    private final UserRepository userRepository;

    @Autowired
    public UserDAOImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity saveUser(UserEntity user) {
        UserEntity userEntity = userRepository.save(user);
        return userEntity;
    }

    @Override
    public UserEntity getUserById(String id) {
        UserEntity userEntity = userRepository.findById(id).orElse(null);
        return userEntity;
    }

    @Override
    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }
}
