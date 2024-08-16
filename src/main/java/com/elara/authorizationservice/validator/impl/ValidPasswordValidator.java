package com.elara.authorizationservice.validator.impl;

import com.elara.authorizationservice.exception.AppException;
import com.elara.authorizationservice.service.MessageService;
import com.elara.authorizationservice.util.GenericRSAUtil;
import com.elara.authorizationservice.validator.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Slf4j
@Component
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Autowired
    MessageService messageService;

    String message;

    @Value("${decrypt.privateKey}")
    String privateKey;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(

                // at least 8 characters
                new LengthRule(8, 30),

                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),

                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),

                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),

                // no whitespace
                new WhitespaceRule()

        ));

        String decrypted;
        try {
            decrypted = GenericRSAUtil.decryptWithPrivateKey(password, privateKey);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }

        RuleResult result = validator.validate(new PasswordData(decrypted));

        boolean valid = result.isValid();

        if (valid) {
            return true;
        }

        throw new AppException(messageService.getMessage(this.message));
    }
}
