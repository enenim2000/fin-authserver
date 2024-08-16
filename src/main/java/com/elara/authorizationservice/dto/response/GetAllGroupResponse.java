package com.elara.authorizationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAllGroupResponse extends BaseResponse {

    private List<Data> data;

    public GetAllGroupResponse() {
        super();
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long id;

        private String companyCode;

        private String groupName;

        private String description;

        private String status;

        private String createdAt;

        private String updatedAt;
    }
}
