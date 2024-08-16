package com.elara.authorizationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAllPermissionsResponse extends BaseResponse {

    private List<Data> data;

    public GetAllPermissionsResponse() {
        super();
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private long id;

        private String permissionId;

        private String permission;

        private String description;

        private String uriPath;

        private boolean isSecured;

        private String status;

        private String createdAt;

        private String updatedAt;
    }

}
