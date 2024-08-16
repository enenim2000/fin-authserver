package com.elara.authorizationservice.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuthorizationGroupResponse {

    private String groupName;

    private String description;

    private String status;

}
