package com.elara.authorizationservice.dto.response;

import com.elara.authorizationservice.enums.ApprovalStatus;
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
public class GetApprovalStatusResponse extends BaseResponse {

    private List<ApprovalStatus> data;
}