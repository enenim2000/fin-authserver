package com.elara.authorizationservice.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class FilterApprovalResponse extends BaseResponse {

    private List<Data> data;
    int pageIndex;
    int pageSize;
    int totalPages;
    boolean hasNextPage;
    boolean hasPreviousPage;
    Long totalContent;

    public FilterApprovalResponse() {
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

        private String companyCode;

        private String approvalItemType;

        private String reference;

        private Integer currentApprovalStage;

        private String bookStatus;

        private String bookedBy;

        private String comment;

        private String customerEmail;

        private String customerPhone;

        private HashMap<Integer, String> approvalActivityLog;

        private HashMap<Integer, String> approvalActivityLogComment;

        private String approvalStatus;

        private Map<Object, Object> body;

        private String createdAt;

        private String updatedAt;

        private String createdBy;

        private String updatedBy;
    }
}
