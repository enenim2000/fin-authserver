package com.elara.authorizationservice.dto.response;

import lombok.Data;

@Data
public class ApprovalPhone {

  protected String phone;
  protected String subject;
  protected String message;

}
