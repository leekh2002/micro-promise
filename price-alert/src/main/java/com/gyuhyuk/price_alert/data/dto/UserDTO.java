package com.gyuhyuk.price_alert.data.dto;

import com.gyuhyuk.price_alert.common.annotation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@PasswordMatch
public class UserDTO {

    private String email;

    private String password;

    private String name;

    @Id
    private String id;
}
