package com.elara.authorizationservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserGroupPermissionRequest {

    private long groupId;

    private List<String> permissionId;

}
