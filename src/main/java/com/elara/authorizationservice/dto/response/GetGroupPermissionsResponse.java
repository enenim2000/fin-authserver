package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class GetGroupPermissionsResponse extends BaseResponse {

    private List<Data> data;

    public GetGroupPermissionsResponse() {
        super();
    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long permissionId;
    }

}
