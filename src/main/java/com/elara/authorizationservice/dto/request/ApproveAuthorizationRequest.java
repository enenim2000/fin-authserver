package com.elara.authorizationservice.dto.request;

import com.elara.authorizationservice.validator.Required;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApproveAuthorizationRequest {

    private Long id;

    @Required(message = "approval.comment.required")
    private String comment;

    @Required(message = "approval.action.required")
    private String approvalAction;
}