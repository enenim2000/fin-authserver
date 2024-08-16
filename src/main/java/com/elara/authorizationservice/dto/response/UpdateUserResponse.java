package com.elara.authorizationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserResponse extends BaseResponse {

    private Data data;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long id;

        private String companyCode;

        private String email;

        private String phone;

        private String lang;

        private String staffName;

        private String userType;

        private boolean isEmailVerified;

        private boolean isPhoneVerified;

        private String status;

        private String createdAt;

        private String updatedAt;
    }
}
