package com.elara.authorizationservice.repository;

import com.elara.authorizationservice.domain.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long>, JpaSpecificationExecutor<Audit> {

    @Query("select mp " +
            "from Audit mp where " +
            "(:approvalItemType IS NULL OR mp.approvalItemType = :approvalItemType) " +
            "AND (:id IS NULL OR mp.id = :id) " +
            "AND (:maker IS NULL OR mp.maker = :maker) " +
            "AND (:status IS NULL OR mp.status = :status) " +
            "AND (:approvalRequired IS NULL OR mp.approvalRequired = :approvalRequired) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR mp.createdAt BETWEEN :startDate AND :endDate) ")
    Page<Audit> searchApprovals(@Param("approvalItemType") String approvalItemType,
                                   @Param("id") Long id,
                                   @Param("maker") String maker,
                                   @Param("status") String status,
                                   @Param("approvalRequired") boolean approvalRequired,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   Pageable pageable);

}
