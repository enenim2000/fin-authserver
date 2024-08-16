package com.elara.authorizationservice.dto.request;

import lombok.Data;

@Data
public class FilterApprovalRequest {

    private String companyCode;

    private String approvalItemType;

    private String reference;

    private Integer currentApprovalStage;

    private String approvalStatus;

    private Boolean allowOnlyLoggedInUser;

    private String startDate;

    private String endDate;

    private int pageIndex;

    private int pageSize;

    public void sanitize() {
        companyCode = companyCode != null && companyCode.trim().equals("") ? null : companyCode;
        approvalItemType = approvalItemType != null && approvalItemType.trim().equals("") ? null : approvalItemType;
        reference = reference != null && reference.trim().equals("") ? null : reference;
        approvalStatus = approvalStatus != null && approvalStatus.trim().equals("") ? null : approvalStatus;
        startDate = startDate != null && startDate.trim().equals("") ? null : startDate;
        endDate = endDate != null && endDate.trim().equals("") ? null : endDate;
    }
}
