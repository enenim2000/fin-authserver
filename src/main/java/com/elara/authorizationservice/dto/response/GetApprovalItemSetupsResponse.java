package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GetApprovalItemSetupsResponse extends BaseResponse {

    private List<Data> data;

    @Builder
    @Getter
    @Setter
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
