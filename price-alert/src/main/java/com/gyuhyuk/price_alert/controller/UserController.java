package com.gyuhyuk.price_alert.controller;

import com.gyuhyuk.price_alert.data.dto.SignUpRequestDTO;
import com.gyuhyuk.price_alert.data.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @PostMapping("/signup")
    public String signup(@Valid @RequestBody SignUpRequestDTO user){

    }
}
