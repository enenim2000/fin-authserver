package com.elara.authorizationservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserGroupRequest {

    private String userId;

    private List<Long> groupIds;

}
