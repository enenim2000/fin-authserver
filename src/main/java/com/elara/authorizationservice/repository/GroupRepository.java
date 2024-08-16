package com.elara.authorizationservice.repository;

import com.elara.authorizationservice.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {

  Group findByGroupNameAndCompanyCode(String groupName, String companyCode);

  @Query("select a from Group a where a.companyCode = :companyCode and a.status = :status")
  List<Group> findByCompanyCode(@Param("companyCode") String companyCode, @Param("status") String status);
}
