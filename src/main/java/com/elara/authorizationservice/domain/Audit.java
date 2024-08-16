package com.elara.authorizationservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Table(name = "au_audit")
@Entity
@Getter
@Setter
public class Audit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Id
    private Long id;

    @Column(name = "_before", columnDefinition = "TEXT")
    private String before;

    @Column(name = "_after", columnDefinition = "TEXT")
    private String after;

    private String action; //Create,Delete,Update

    private String description; //Created new Staff

    private String entity; //table affected

    private Long entityId;

    private String ipAddress;

    private String userAgent;

    private String userType;

    private String approvalItemType;

    private String responseType;

    private boolean approvalRequired;

    @Column(name = "approvalEmail", length = 4000)
    private String approvalEmail;

    @Column(name = "approvalSms", length = 4000)
    private String approvalSms;

    @Column(name = "approvalDependency", length = 4000)
    private String approvalDependency;

    private String status; //Approved,Pending,Rejected,Recall

    private String comment;

    private String maker; //Creator

    private String checker; //Approver

    private Date createdAt;

    private Date updatedAt;

    private String createdBy; //email

    private String updatedBy; //email
}
