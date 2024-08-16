package com.elara.authorizationservice.dto.approval;

import com.elara.authorizationservice.enums.ApprovalStatus;
import lombok.Data;

@Data
public class Loan {
    private String companyCode;
    private String loginId;
    private String customerId;
    private String loanProductCode;
    private double loanAmount;
    private int tenure;
    private int moratium;
    private String purpose;
    private boolean termsAndConditionAccepted;
    private String disbursementAccount;
    private String loanAccount;
    private boolean hasExistingLoanWithOtherInstitution;
    private String homeOwner;
    private int noOfDependent;
    private double monthlyIncome;
    private String businessName;
    private String businessEmail;
    private String businessAddress;
    private String businessRole;
    private String bankStatement;
    private String cac;
    private String govtIssuedId;
    private String utilityBill;
    private String salaryStatement;
    private String guarantor;
    private String guarantorIdCard;
    private String guarantorPassport;
    private String workIdCard;
    private String confirmationLetter;
    private String remitaApplicationEvidence;
    private int loanTenure;
    private String transactionTrackingRef; //bank one reference

    private double interestRate; //in percentage
    private Integer computationMode;
    private Integer PrincipalPaymentFrequency;
    private Integer InterestPaymentFrequency;
    private String termsAndCondition;
    private Integer maxLoanTenure;

    private String reference; //auto generated uuid

    private String comment; //comment from customer
    private String remark; // comment from staff
    private boolean editable; //true or false
    private ApprovalStatus approvalStatus;
}
