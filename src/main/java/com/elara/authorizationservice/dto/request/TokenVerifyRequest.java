package com.elara.authorizationservice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenVerifyRequest {

    //Client id of the service on application table
    private String serviceClientId;
    private String token;
    private String permissionId;
    private String requestSource;
    private String username;
    private boolean secured;
}
