package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class UserPermissionAndGroupResponse extends BaseResponse {

    private Data data;

    public UserPermissionAndGroupResponse () {
        super();
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private List<UserPermission> userPermissions;
        private List<UserGroup> userGroups;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPermission {
        private Long id;
        private String companyCode;
        private long userId;
        private String permissionId;
        private String createdAt;
        private String updatedAt;
        private String createdBy;
        private String updatedBy;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGroup {
        private Long id;
        private String companyCode;
        private long userId;
        private long groupId;
        private String createdAt;
        private String updatedAt;
        private String createdBy;
        private String updatedBy;
    }
}
