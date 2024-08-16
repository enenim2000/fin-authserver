package com.elara.authorizationservice.dto.request;

import com.elara.authorizationservice.validator.Required;
import com.elara.authorizationservice.validator.ValidPassword;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String otp;
    private String username;

    @Required(message = "password.required")
    @ValidPassword(message = "password.valid")
    private String newPassword;

    private String confirmPassword;

}
