package com.gyuhyuk.micro_promise.service;


import com.gyuhyuk.micro_promise.data.dto.JoinDTO;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.data.entity.UserRole;
import com.gyuhyuk.micro_promise.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinDTO joinDTO) {

        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String name = joinDTO.getName();

        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {

            return;
        }

        UserEntity data = new UserEntity();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setName(name);
        data.setRole(UserRole.ROLE_USER);

        userRepository.save(data);
    }
}