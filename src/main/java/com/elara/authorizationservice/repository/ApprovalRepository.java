package com.elara.authorizationservice.repository;

import com.elara.authorizationservice.domain.Approval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long>, JpaSpecificationExecutor<Approval> {

    @Query("select a from Approval a where a.companyCode = :companyCode and (:approvalItemType IS NULL or a.approvalItemType = :approvalItemType) and (:approvalStatus IS NULL or a.approvalStatus = :approvalStatus)")
    List<Approval> findByCompanyCodeAndApprovalItemTypeAndApprovalStatus(@Param("companyCode") String companyCode, @Param("approvalItemType")String approvalItemType, @Param("approvalStatus")String approvalStatus);

    @Query("select a from Approval a where a.mailToNextApprovalRequired = :mailToNextApprovalRequired or a.mailToCustomerRequired = :mailToCustomerRequired")
    List<Approval> findNotificationPending(@Param("mailToNextApprovalRequired") boolean mailToNextApprovalRequired, @Param("mailToCustomerRequired") boolean mailToCustomerRequired);

    Approval findByReference(String reference);

    @Query("select mp " +
            "from Approval mp where " +
            "(:companyCode IS NULL OR mp.companyCode = :companyCode) " +
            "AND (:approvalItemType IS NULL OR mp.approvalItemType = :approvalItemType) " +
            "AND (:reference IS NULL OR mp.reference = :reference) " +
            "AND (:currentApprovalStage IS NULL OR mp.currentApprovalStage = :currentApprovalStage) " +
            "AND (:approvalStatus IS NULL OR mp.approvalStatus = :approvalStatus) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR mp.createdAt BETWEEN :startDate AND :endDate) ")
    Page<Approval> searchApprovals(@Param("companyCode") String companyCode,
                                                 @Param("approvalItemType") String approvalItemType,
                                                 @Param("reference") String reference,
                                                 @Param("currentApprovalStage") Integer currentApprovalStage,
                                                 @Param("approvalStatus") String approvalStatus,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 Pageable pageable);
}
