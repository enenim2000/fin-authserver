package com.elara.authorizationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewSettingsKeyValuesResponse extends BaseResponse {

    private Map<String, Object> data;

    public ViewSettingsKeyValuesResponse() {
        super();
    }
}
