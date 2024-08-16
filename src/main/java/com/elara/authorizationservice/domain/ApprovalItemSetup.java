package com.elara.authorizationservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Table(name = "au_approval_item_setup", indexes = {
        @Index(name = "approvalItemTypeIndex", columnList = "approvalItemType"),
        @Index(name = "approvalItemTypeCompanyCodeIndex", columnList = "companyCode,approvalItemType")
})
@Entity
@Getter
@Setter
public class ApprovalItemSetup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Id
    private Long id;

    @Column(name = "companyCode")
    private String companyCode;

    @Column(name = "approvalItemType")
    private String approvalItemType;

    @Column(name = "approvalLevels")
    private int approvalLevels;

    @Column(name = "approvalStageStaffIds", length = 4000)
    private String approvalStageStaffIds;

    @Column(name = "approvalStageAmounts", length = 4000)
    private String approvalStageAmounts;

    @Column(name = "createdAt", nullable = false)
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "updatedBy")
    private String updatedBy;

    @Column(name = "status", nullable = false)
    private String status;
}
