package com.gyuhyuk.price_alert.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gyuhyuk.price_alert.common.annotation.PasswordMatch;
import com.gyuhyuk.price_alert.data.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserDTO {

    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Size(min = 3, max = 100)
    private String password;

    private String name;

    @Id
    private String id;

    private Set<AuthorityDTO> authorityDtoSet;

    public static UserDTO from(UserEntity user) {
        if(user == null) return null;

        return UserDTO.builder()
                .id(user.getUsername())
                .email(user.getEmail())
                .name(user.getUsername())
                .authorityDtoSet(user.getAuthorities().stream()
                        .map(authority -> AuthorityDTO.builder().authorityName(authority.getAuthorityName()).build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
