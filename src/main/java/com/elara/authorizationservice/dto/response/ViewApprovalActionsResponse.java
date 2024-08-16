package com.elara.authorizationservice.dto.response;

import com.elara.authorizationservice.enums.ApprovalAction;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewApprovalActionsResponse extends BaseResponse {

    private List<ApprovalAction> data;
}
