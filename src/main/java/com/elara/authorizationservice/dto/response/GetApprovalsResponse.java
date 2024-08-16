package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class GetApprovalsResponse extends BaseResponse {

    private List<Data> data;

    public GetApprovalsResponse() {
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

        private String approvalActivityLog;

        private String approvalStatus;

        private String bookStatus;

        private String bookedBy;

        private String comment;

        private String customerEmail;

        private String customerPhone;

        private Map<Object, Object> body;

        private String createdAt;

        private String updatedAt;

        private String createdBy;

        private String updatedBy;
    }
}
