package com.elara.authorizationservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyRequest {

    private String companyName;

    private String companyAddress;

}
