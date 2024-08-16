package com.elara.authorizationservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "au_user", indexes = {
        @Index(name = "phoneNumberIndex", columnList = "phone"),
                @Index(name = "emailIndex", columnList = "email"),
                @Index(name = "emailPhoneIndex", columnList = "email,phone"),
                @Index(name = "companyCodeEmailPhoneIndex", columnList = "companyCode,email,phone"),
                @Index(name = "companyCodeUserTypeStatusIndex", columnList = "companyCode,userType,status")
})
@AllArgsConstructor
@NoArgsConstructor
public class User extends AuditBaseModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Id
    private Long id;

    @Column(name = "companyCode")
    private String companyCode;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "lang")
    private String lang;

    @Column(name = "userType")
    private String userType;

    @Column(name = "staffName")
    private String staffName;

    @Column(name = "hasChangedPassword")
    private Boolean hasChangedPassword;

    @Column(name = "isEmailVerified")
    private boolean isEmailVerified;

    @Column(name = "isPhoneVerified")
    private boolean isPhoneVerified;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "loginAttemptCount")
    private Integer loginAttemptCount = 0;

    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "updatedBy")
    private String updatedBy;
}
