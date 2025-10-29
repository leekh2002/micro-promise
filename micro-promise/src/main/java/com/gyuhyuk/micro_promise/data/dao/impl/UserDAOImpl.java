package com.gyuhyuk.micro_promise.data.dao.impl;

import com.gyuhyuk.micro_promise.data.dao.UserDAO;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
