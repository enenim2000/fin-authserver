package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class GetAllStaffResponse extends BaseResponse {

    private List<Data> data;

    public GetAllStaffResponse() {
        super();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Data {
        private Long id;

        private String companyCode;

        private String email;

        private String phone;

        private String lang;

        private String userType;

        private String staffName;

        private boolean hasChangedPassword;

        private boolean isEmailVerified;

        private boolean isPhoneVerified;

        private String status;

        private String createdAt;

        private String updatedAt;

        private String createdBy;

        private String updatedBy;
    }
}
