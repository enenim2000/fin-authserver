package com.elara.authorizationservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class SaveSystemSettingResponse extends BaseResponse {

    public SaveSystemSettingResponse() {
        super();
    }
}
