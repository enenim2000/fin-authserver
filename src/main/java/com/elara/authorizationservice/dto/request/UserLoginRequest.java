package com.elara.authorizationservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    private String username;

    private String password;

}
