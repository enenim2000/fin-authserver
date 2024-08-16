package com.elara.authorizationservice.dto.request;

import com.elara.authorizationservice.validator.Required;
import com.elara.authorizationservice.validator.ValidEmail;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @Required(message = "companyCode.required")
    private String companyCode;

    @Required(message = "email.required")
    @ValidEmail(message = "email.valid")
    private String email;

    @Required(message = "phone.required")
    private String phone;

    @Required(message = "staffName.required")
    private String staffName;

    @Required(message = "lang.required")
    private String lang;

    private String userType;
}
