package com.elara.authorizationservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ApprovalMail {

  protected String email;
  protected String subject;
  protected String message;

}
