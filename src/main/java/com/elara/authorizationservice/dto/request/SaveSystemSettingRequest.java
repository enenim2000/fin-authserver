package com.elara.authorizationservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveSystemSettingRequest {

    private String key;

    private String value;
}
