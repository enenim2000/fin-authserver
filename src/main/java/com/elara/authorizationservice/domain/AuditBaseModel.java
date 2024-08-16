package com.elara.authorizationservice.domain;

import com.elara.authorizationservice.dto.response.ApprovalDependency;
import com.elara.authorizationservice.dto.response.ApprovalMail;
import com.elara.authorizationservice.dto.response.ApprovalPhone;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import jakarta.persistence.Transient;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuditBaseModel {

    @Transient
    @JsonIgnore
    @Expose(serialize = false, deserialize = false)
    public Boolean skipAudit = false;

    @Transient
    @JsonIgnore
    @Expose(serialize = false, deserialize = false)
    public Boolean skipAuthorization = false;

    @Transient
    @JsonIgnore
    @Expose(serialize = false, deserialize = false)
    public String before;

    @Transient
    @JsonIgnore
    @Expose(serialize = false, deserialize = false)
    public String responseType;

    @Transient
    @JsonIgnore
    @Expose(serialize = false, deserialize = false)
    public List<ApprovalMail> approvalMails = new ArrayList<>();

    @Transient
    @JsonIgnore
    @Expose(serialize = false, deserialize = false)
    public List<ApprovalPhone> approvalSms = new ArrayList<>();

    @Transient
    @JsonIgnore
    @Expose(serialize = false, deserialize = false)
    public List<ApprovalDependency> approvalDependencies = new ArrayList<>();

    public void addApprovalMail(ApprovalMail approvalMail) {
        this.approvalMails.add(approvalMail);
    }

    public void addApprovalPhone(ApprovalPhone approvalPhone) {
        this.approvalSms.add(approvalPhone);
    }

    public void addApprovalDependency(ApprovalDependency approvalDependency) {
        this.approvalDependencies.add(approvalDependency);
    }
}
