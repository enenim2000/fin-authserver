package com.elara.authorizationservice.repository;

import com.elara.authorizationservice.domain.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long>, JpaSpecificationExecutor<SystemSetting> {

  SystemSetting findByCompanyCodeAndName(String companyCode, String name);
  List<SystemSetting> findByCompanyCode(String companyCode);
}
