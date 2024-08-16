package com.elara.authorizationservice.domain;

import com.elara.authorizationservice.enums.BookStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Table(name = "au_approval", indexes = {
        @Index(name = "approvalItemTypeCompanyCodeApprovalStatusIndex", columnList = "companyCode,approvalItemType,approvalStatus"),
        @Index(name = "approvalItemTypeCompanyCodeIndex", columnList = "companyCode,approvalItemType")
})
@Entity
@Getter
@Setter
public class Approval implements Serializable {

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

    @Column(name = "approvalAction")
    private String approvalAction; //DELETE,CREATE,UPDATE

    @Column(name = "approvalRequestJson", length = 4500)
    private String approvalRequestJson;

    @Column(name = "reference", unique = true)
    private String reference;

    @Column(name = "currentApprovalStage")
    private int currentApprovalStage;

    @Column(name = "approvalActivityLog", length = 2000)
    private String approvalActivityLog; //e.g {"1":"enenim2000@gmail.com", "2":"daniel.bajomo@elara-solutions.com"}

    @Column(name = "approvalActivityLogComment", length = 2000)
    private String approvalActivityLogComment; //e.g {"1":"Pls upload Guarantor photo", "2":"Approved", "3":"Upload bank statement"}

    @Column(name = "approvalStatus")
    private String approvalStatus;

    @Column(name = "bookStatus")
    private String bookStatus = BookStatus.Open.name();

    @Column(name = "bookedBy")
    private String bookedBy;

    @Column(name = "comment")
    private String comment;

    @Column(name = "customerEmail")
    private String customerEmail;

    @Column(name = "customerPhone")
    private String customerPhone;

    @Column(name = "mailToNextApprovalRequired")
    private boolean mailToNextApprovalRequired;

    @Column(name = "mailToCustomerRequired")
    private boolean mailToCustomerRequired;

    @Column(name = "createdAt", nullable = false)
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "updatedBy")
    private String updatedBy;
}
