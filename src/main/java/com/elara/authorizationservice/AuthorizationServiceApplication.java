package com.elara.authorizationservice;

import com.elara.authorizationservice.dto.request.SyncPermissionRequest;
import com.elara.authorizationservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@EnableJpaRepositories({"com.elara.authorizationservice.repository"})
@ComponentScan({"com.elara.authorizationservice"})
@EnableScheduling
@SpringBootApplication
public class AuthorizationServiceApplication extends SpringBootServletInitializer implements CommandLineRunner {

	@Autowired
	PermissionService permissionService;

	@Autowired
	UserService userService;

	@Autowired
	ApplicationService applicationService;

	@Autowired
	CompanyService companyService;

	@Autowired
	SystemSettingService systemSettingService;

	@Autowired
	RequestMappingHandlerMapping requestMappingHandlerMapping;

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationServiceApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(AuthorizationServiceApplication.class);
	}

	@Override
	public void run(String... args) throws Exception {
		userService.createDefaultSystemAdmin();
		companyService.createDefaultCompany();
		applicationService.createDefaultApplications();
		permissionService.createDefaultGroup();
		SyncPermissionRequest syncPermissionRequest = permissionService.generatePermissionRequest(requestMappingHandlerMapping);
		permissionService.syncApplicationPermission(syncPermissionRequest);
		//Move country table
		//Move disco table
	}
}
