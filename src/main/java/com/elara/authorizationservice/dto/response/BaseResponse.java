package com.elara.authorizationservice.dto.response;

import com.elara.authorizationservice.auth.RequestUtil;
import com.elara.authorizationservice.enums.ResponseCode;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class BaseResponse {

  protected String responseCode;
  protected String responseMessage;

  public BaseResponse() {
    String message = RequestUtil.getApprovalMessage() != null ? RequestUtil.getApprovalMessage() : RequestUtil.getMessage();
    this.responseCode = ResponseCode.SUCCESSFUL.getValue();
    this.responseMessage = message == null || message.trim().equals("") ? "Successful" : message;
  }
}
