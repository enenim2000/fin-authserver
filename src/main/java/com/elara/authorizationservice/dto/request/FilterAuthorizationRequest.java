package com.elara.authorizationservice.dto.request;

import lombok.Data;

@Data
public class FilterAuthorizationRequest {

    private Long id;

    private String companyCode;

    private String approvalItemType;

    private String approvalStatus;

    private Boolean includeLoggedInUser;

    private String startDate;

    private String endDate;

    private int pageIndex;

    private int pageSize;

    public void sanitize() {
        companyCode = companyCode != null && companyCode.trim().equals("") ? null : companyCode;
        approvalItemType = approvalItemType != null && approvalItemType.trim().equals("") ? null : approvalItemType;
        approvalStatus = approvalStatus != null && approvalStatus.trim().equals("") ? null : approvalStatus;
        startDate = startDate != null && startDate.trim().equals("") ? null : startDate;
        endDate = endDate != null && endDate.trim().equals("") ? null : endDate;
    }
}
