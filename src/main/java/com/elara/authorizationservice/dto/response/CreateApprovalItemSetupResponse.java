package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class CreateApprovalItemSetupResponse extends BaseResponse {

    private Data data;

    public CreateApprovalItemSetupResponse() {
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

        private String approvalLevels;

        private HashMap<Integer, List<Long>> approvalStageStaffIds;

        private HashMap<Integer, Double> approvalStageAmounts;

        private String createdAt;

        private String updatedAt;

        private String createdBy;

        private String updatedBy;

        private String status;
    }

}
