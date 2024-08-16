package com.elara.authorizationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterAuthorizationResponse extends BaseResponse {

    private List<Data> data;
    int pageIndex;
    int pageSize;
    int totalPages;
    boolean hasNextPage;
    boolean hasPreviousPage;
    Long totalContent;

    public FilterAuthorizationResponse() {
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
