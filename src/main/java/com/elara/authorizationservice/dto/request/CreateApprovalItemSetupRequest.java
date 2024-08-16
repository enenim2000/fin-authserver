package com.elara.authorizationservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateApprovalItemSetupRequest {

    private String approvalItemType; //Loan

    private Integer approvalLevels; //5

    private String approvalStageStaffIds; //{1:[2,3],2:[5,6]}

    private String approvalStageAmounts; //{1:10.0,2:200.0}

}
