package com.elara.authorizationservice.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SaveAllSystemSettingRequest {

    private List<Data> data;

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String key;
        private String value;
    }
}
