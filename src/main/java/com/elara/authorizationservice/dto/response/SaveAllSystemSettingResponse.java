package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveAllSystemSettingResponse extends BaseResponse {

    private List<Data> data;

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String key;
        private String value;
    }

}
