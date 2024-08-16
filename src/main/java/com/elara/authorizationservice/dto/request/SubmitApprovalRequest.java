package com.elara.authorizationservice.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubmitApprovalRequest {

    private String companyCode;

    private String approvalItemType;

    private String approvalRequestJson;

    private String reference;

    private String comment;

    private String customerEmail;

    private String customerPhone;
}
