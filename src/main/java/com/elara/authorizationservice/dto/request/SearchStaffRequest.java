package com.elara.authorizationservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SearchStaffRequest {

        private String searchTerm;
        private String staffName;
        private String email;
        private String phone;

        private String status;

        private String startDate;

        private String endDate;

        private int pageIndex;

        private int pageSize;

        public void sanitize() {
                searchTerm = searchTerm != null && searchTerm.trim().equals("") ? null : searchTerm;
                staffName = staffName != null && staffName.trim().equals("") ? null : staffName;
                email = email != null && email.trim().equals("") ? null : email;
                status = status != null && status.trim().equals("") ? null : status;
                phone = phone != null && phone.trim().equals("") ? null : phone;
                startDate = startDate != null && startDate.trim().equals("") ? null : startDate;
                endDate = endDate != null && endDate.trim().equals("") ? null : endDate;
        }
}
