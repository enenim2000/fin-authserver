package com.elara.authorizationservice.dto.response;

import com.elara.authorizationservice.domain.ApplicationPermission;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupPermissionResponse extends BaseResponse{

    private long groupId;

    private List<ApplicationPermission> permissions;

}
