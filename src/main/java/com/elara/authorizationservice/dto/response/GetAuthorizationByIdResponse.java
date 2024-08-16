package com.elara.authorizationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAuthorizationByIdResponse extends BaseResponse {

    Data data;

    public GetAuthorizationByIdResponse() {
        super();
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        private Long id;

        private String approvalItemType;

        private String action;

        private String description;

        private String entity;

        private String status;

        private String comment;

        private String maker;

        private String checker;

        private Object initialState;

        private Object finalState;

        private boolean approvalRequired;

        private String createdAt;

        private String updatedAt;

        private String createdBy;

        private String updatedBy;
    }
}
