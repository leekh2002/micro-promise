package com.gyuhyuk.price_alert.controller;

import com.gyuhyuk.price_alert.data.dto.SignUpRequestDTO;
import com.gyuhyuk.price_alert.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpRequestDTO> signup(@Valid @RequestBody SignUpRequestDTO user){
        userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
