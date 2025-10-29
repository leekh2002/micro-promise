package com.gyuhyuk.micro_promise.common.valid;

import com.gyuhyuk.micro_promise.common.annotation.PasswordMatch;
import com.gyuhyuk.micro_promise.data.dto.SignUpRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, SignUpRequestDTO> {

    @Override
    public boolean isValid(SignUpRequestDTO dto, ConstraintValidatorContext context) {
        return dto.getPassword().equals(dto.getConfirmPassword());
    }
}
