package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class ApproveRequestResponse extends BaseResponse {

    private Data data;

    public ApproveRequestResponse() {
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

        private String reference;

        private String currentApprovalStage;

        private HashMap<Integer, String> approvalActivityLog;

        private HashMap<Integer, String> approvalActivityLogComment;

        private boolean specialApprovalRequired;

        private boolean specialApprovalGiven;

        private String approvalStatus;

        private String comment;

        private String bookStatus;

        private String bookedBy;

        private String customerEmail;

        private String customerPhone;

        private Map<Object, Object> body;

        private String createdAt;

        private String updatedAt;

        private String createdBy;

        private String updatedBy;
    }
}
