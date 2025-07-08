package com.gyuhyuk.price_alert.service.impl;

import com.gyuhyuk.price_alert.data.dao.UserDAO;
import com.gyuhyuk.price_alert.data.dto.SignUpRequestDTO;
import com.gyuhyuk.price_alert.data.dto.UserDTO;
import com.gyuhyuk.price_alert.data.entity.UserEntity;
import com.gyuhyuk.price_alert.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDTO getUser(String userId) {
        UserEntity userEntity = userDAO.getUserById(userId);

        UserDTO userDTO = new UserDTO(userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getName(),
                userEntity.getId());

        return userDTO;
    }

    @Override
    @Transactional
    public UserDTO saveUser(SignUpRequestDTO user) {
        System.out.println(userDAO.getUserById(user.getId()));
        if (userDAO.getUserById(user.getId()) != null) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다: " + user.getId());
        }

        UserEntity userEntity = new UserEntity(user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getName(),
                LocalDateTime.now());

        UserEntity userEntityResult = userDAO.saveUser(userEntity);

        return new UserDTO(userEntityResult.getEmail(),
                userEntityResult.getPassword(),
                userEntityResult.getName(),
                userEntityResult.getId());
    }
}
