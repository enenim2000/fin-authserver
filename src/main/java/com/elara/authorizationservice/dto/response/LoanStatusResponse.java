package com.elara.authorizationservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class LoanStatusResponse extends BaseResponse {

    private Data data;

    public LoanStatusResponse() {
        super();
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        private String approvalItemType;

        private String reference;

        private Integer currentApprovalStage;

        private String approvalStatus;

        private String comment;
    }
}

