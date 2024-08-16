package com.elara.authorizationservice.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class InitiateOtpResponse extends BaseResponse {

    public InitiateOtpResponse() {
        super();
    }

    private Data data;

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String otpHash;
    }
}
