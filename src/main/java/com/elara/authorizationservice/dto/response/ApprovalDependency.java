package com.elara.authorizationservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ApprovalDependency {

  protected String className; //UserLogin
  protected String fieldName; //userId

}
