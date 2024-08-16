package com.elara.authorizationservice.dto.request;

import com.elara.authorizationservice.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationVerifyRequest {
    private String otp;
    private NotificationType notificationType;
}

