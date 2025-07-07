package com.gyuhyuk.price_alert.common.valid;

import com.gyuhyuk.price_alert.common.annotation.PasswordMatch;
import com.gyuhyuk.price_alert.data.dto.SignUpRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, SignUpRequestDTO> {

    @Override
    public boolean isValid(SignUpRequestDTO dto, ConstraintValidatorContext context) {
        return dto.getPassword().equals(dto.getConfirmPassword());
    }
}
