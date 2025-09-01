package com.gyuhyuk.price_alert.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import com.gyuhyuk.price_alert.common.exception.DuplicateMemberException;
import com.gyuhyuk.price_alert.common.exception.NotFoundMemberException;
import com.gyuhyuk.price_alert.data.dto.UserDTO;
import com.gyuhyuk.price_alert.data.entity.AuthorityEntity;
import com.gyuhyuk.price_alert.data.entity.UserEntity;
import com.gyuhyuk.price_alert.repository.UserRepository;
import com.gyuhyuk.price_alert.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO signup(UserDTO userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getId()).orElse(null) != null) {
            throw new DuplicateMemberException("이미 가입되어 있는 유저입니다.");
        }

        AuthorityEntity authority = AuthorityEntity.builder()
                .authorityName("ROLE_USER")
                .build();

        UserEntity user = UserEntity.builder()
                .id(userDto.getId())
                .username(userDto.getName())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .createdAt(LocalDateTime.now())
                .build();

        return UserDTO.from(userRepository.save(user));
    }

    // username을 기준으로 정보를 가져옴
    @Transactional(readOnly = true)
    public UserDTO getUserWithAuthorities(String username) {
        return UserDTO.from(userRepository.findOneWithAuthoritiesByUsername(username).orElse(null));
    }

    // SecuityContext에 저장된 username의 정보만 가져옴
    @Transactional(readOnly = true)
    public UserDTO getMyUserWithAuthorities() {
        return UserDTO.from(
                SecurityUtil.getCurrentUsername()
                        .flatMap(userRepository::findOneWithAuthoritiesByUsername)
                        .orElseThrow(() -> new NotFoundMemberException("Member not found"))
        );
    }
}