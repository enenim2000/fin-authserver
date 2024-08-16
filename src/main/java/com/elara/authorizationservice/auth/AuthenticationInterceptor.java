package com.elara.authorizationservice.auth;

import com.elara.authorizationservice.domain.Company;
import com.elara.authorizationservice.domain.SystemSetting;
import com.elara.authorizationservice.domain.User;
import com.elara.authorizationservice.dto.request.TokenVerifyRequest;
import com.elara.authorizationservice.dto.response.TokenVerifyResponse;
import com.elara.authorizationservice.enums.*;
import com.elara.authorizationservice.exception.AppException;
import com.elara.authorizationservice.repository.CompanyRepository;
import com.elara.authorizationservice.repository.UserRepository;
import com.elara.authorizationservice.service.AuthenticationService;
import com.elara.authorizationservice.service.MessageService;
import com.elara.authorizationservice.service.SystemSettingService;
import com.elara.authorizationservice.util.HashUtil;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    /**
     * Block and Unblock user or customer, update pin when clicked by admin reset to default 0000, delete pin
     * Block and unblock loans
     * shutdown entire API centrally for all client or specific clients
     * dashboard report summary
     * Payment history
     * Loan
     * Different type of loans
     * select loan to apply
     * Check the credit history, and other eligibility conditions
     * upload guarantor, ID, passport, etc
     * allow to apply for loan
     * conditional approval
     * Customer must accept
     * Each approval level on the loan activity has comments for each person that approve
     * When disburse is click we send the request to bankone to disburse to customer account
     * Send loan for approval request, by several people, if condition greater than 200,000 force certain level like MD approval included
     * Pick up loan and approval management system
     * Loan dashboard summary, no of approval volume approval amount,
     */

    private String authServerClientId;

    private String serviceName;

    private final AuthenticationService authenticationService;

    private final CompanyRepository companyRepository;

    private final SystemSettingService systemSettingService;

    private final MessageService messageService;
    private final UserRepository userRepository;

    public AuthenticationInterceptor(AuthenticationService authenticationService,
                                     CompanyRepository companyRepository,
                                     UserRepository userRepository,
                                     MessageService messageService,
                                     SystemSettingService systemSettingService) {
        this.authenticationService = authenticationService;
        this.companyRepository = companyRepository;
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.systemSettingService = systemSettingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        RequestUtil.setMessage(null); //For Authorization or approval flow
        RequestUtil.setApprovalMessage(null); //For Authorization or approval flow
        String clientId = request.getHeader("client-id");
        String requestSource = request.getHeader("request-source");
        String username = request.getHeader("username");

        if (requestSource == null || requestSource.trim().isEmpty()) {
            throw new AppException(messageService.getMessage("Channel.Empty"));
        }

        Company company = companyRepository.findByClientId(clientId);
        if (company == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        if (!EntityStatus.Enabled.name().equals(company.getStatus())) {
            throw new AppException(messageService.getMessage("Company.Account.Disabled"));
        }

        RequestUtil.setAuthToken(new AuthToken());
        RequestUtil.getAuthToken().setCompanyCode(company.getCompanyCode());
        RequestUtil.getAuthToken().setCompanyName(company.getCompanyName());
        RequestUtil.setChannel(requestSource);

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequestUtil.setSettings(systemSettingService.getSettingMap());

        SystemSetting systemSetting = RequestUtil.getSettings().get(SystemSettingKeys.PLATFORM_STATE.getValue());

        SystemSetting systemSettingMobileChannel = RequestUtil.getSettings().get(SystemSettingKeys.MOBILE_CHANNEL_STATE.getValue());

        SystemSetting systemSettingAdminPortalChannel = RequestUtil.getSettings().get(SystemSettingKeys.PLATFORM_BACKEND_CHANNEL_STATE.getValue());

        log.info("***** username: {}", username);

        User user = userRepository.findByUsername(username);
        log.info("**** user: {}", new Gson().toJson(user));

        boolean superAdmin;
        if (RequestSource.Service.name().equalsIgnoreCase(requestSource)) {
            superAdmin = true;
        } else {
            superAdmin = user != null && (GroupType.Admin.name().equals(user.getUserType()) || GroupType.SuperAdmin.name().equals(user.getUserType()));
        }

        if (PlatformState.Shutdown.name().equals(systemSetting.getValue()) && !superAdmin) {
            throw new AppException(messageService.getMessage("Platform.State.Shutdown"));
        }

        if (PlatformState.Maintenance.name().equals(systemSetting.getValue()) && !superAdmin) {
            throw new AppException(messageService.getMessage("Platform.State.Maintenance"));
        }

        if (EntityStatus.Disabled.name().equals(company.getStatus()) && !superAdmin) {
            throw new AppException(messageService.getMessage("Company.Account.Disabled"));
        }

        if (RequestSource.Mobile.getValue().equalsIgnoreCase(requestSource) && systemSettingMobileChannel != null && !PlatformState.Active.name().equals(systemSettingMobileChannel.getValue())) {
            throw new AppException(messageService.getMessage("Platform.State.Maintenance"));
        }

        if (RequestSource.PlatformBackend.getValue().equalsIgnoreCase(requestSource) && systemSettingAdminPortalChannel != null && !PlatformState.Active.name().equals(systemSettingAdminPortalChannel.getValue()) && !superAdmin) {
            throw new AppException(messageService.getMessage("Platform.State.Maintenance"));
        }

        if (!isSecuredRoute(handlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || token.trim().equalsIgnoreCase("")) {
            throw new AppException(messageService.getMessage("Token.Required"));
        }
        token = token.replace("Bearer ", "");

        String permissionId = HashUtil.getHash(serviceName + handlerMethod.getMethod().getDeclaredAnnotation(Permission.class).value());
       TokenVerifyResponse result = authenticationService.verifyToken(TokenVerifyRequest.builder()
                       .token(token)
                       .serviceClientId(authServerClientId)
                       .permissionId(permissionId)
                       .secured(handlerMethod.getMethod().isAnnotationPresent(Permission.class))
                       .requestSource(requestSource)
                       .username(username)
                       .build());

       if (!ResponseCode.SUCCESSFUL.getValue().equals(result.getResponseCode())) {
           throw new AppException(result.getResponseMessage());
       }

       RequestUtil.getAuthToken().setUsername(result.getData().getUsername());
       RequestUtil.getAuthToken().setUuid(result.getData().getLoginId());
       RequestUtil.getAuthToken().setEmail(result.getData().getEmail());
       RequestUtil.getAuthToken().setPhone(result.getData().getPhone());
       RequestUtil.getAuthToken().setPhoneVerified(result.getData().isPhoneVerified());
       RequestUtil.getAuthToken().setEmailVerified(result.getData().isEmailVerified());
       RequestUtil.getAuthToken().setHasChangedPassword(result.getData().isHasChangedPassword());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    private boolean isSecuredRoute(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().isAnnotationPresent(
            Permission.class);
    }

    public void setAuthServerClientId(String authServerClientId) {
        this.authServerClientId = authServerClientId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
