package com.gyuhyuk.price_alert.service;

import com.gyuhyuk.price_alert.data.dto.SignUpRequestDTO;
import com.gyuhyuk.price_alert.data.dto.UserDTO;
import com.gyuhyuk.price_alert.data.entity.UserEntity;
import jakarta.validation.Valid;

public interface UserService {
    UserDTO saveUser(SignUpRequestDTO user);
    UserDTO getUser(String userId);
}
