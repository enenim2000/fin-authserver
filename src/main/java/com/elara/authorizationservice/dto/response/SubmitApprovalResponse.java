package com.elara.authorizationservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class SubmitApprovalResponse extends BaseResponse {

    private Data data;

    public SubmitApprovalResponse() {
        super();
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        private Long id;

        private String companyCode;

        private String approvalItemType;

        private String approvalAction;

        private String approvalRequestJson;

        private String reference;

        private int currentApprovalStage;

        private String approvalStatus;

        private String comment;

        private String customerEmail;

        private String customerPhone;

        private String bookStatus;

        private String bookedBy;

        private String createdAt;

        private String updatedAt;

        private String createdBy;

        private String updatedBy;
    }
}
