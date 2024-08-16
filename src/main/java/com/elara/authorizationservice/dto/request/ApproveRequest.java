package com.elara.authorizationservice.dto.request;

import com.elara.authorizationservice.enums.ApprovalAction;
import com.elara.authorizationservice.validator.Required;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApproveRequest {

    @Required(message = "approval.comment.required")
    private String comment;

    @Required(message = "approval.approvalReference.required")
    private String approvalReference;

    @Required(message = "approval.action.required")
    private ApprovalAction approvalAction;
}