package com.elara.authorizationservice.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuthorizationUserResponse {

    private String email;

    private String phone;

    private String lang;

    private String staffName;

    private String userType;

    private String status;

}
