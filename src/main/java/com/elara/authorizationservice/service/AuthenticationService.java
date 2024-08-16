package com.elara.authorizationservice.service;

import com.elara.authorizationservice.auth.AuthToken;
import com.elara.authorizationservice.auth.RequestUtil;
import com.elara.authorizationservice.domain.*;
import com.elara.authorizationservice.dto.request.*;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.enums.*;
import com.elara.authorizationservice.exception.AppException;
import com.elara.authorizationservice.repository.*;
import com.elara.authorizationservice.util.*;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class AuthenticationService {

  final UserRepository userRepository;
  final UserService userService;
  final CompanyRepository companyRepository;
  final GroupRepository groupRepository;
  final UserGroupRepository userGroupRepository;
  final UserGroupPermissionRepository userGroupPermissionRepository;
  final UserLoginRepository userLoginRepository;
  final ApplicationAccountRepository applicationAccountRepository;
  final ModelMapper modelMapper;
  final MessageService messageService;
  final NotificationService notificationService;
  final SystemSettingService systemSettingService;
  final PasswordEncoder passwordEncoder;
  final JWTTokens jwtTokens;
  final ApplicationService applicationService;
  final UserGroupService userGroupService;
  final NotificationCacheService notificationCacheService;

  @Value("${app.mail.sender}")
  String senderMail;

  @Value("${app.sms.sender}")
  String senderSms;

  @Value("${spring.application.name}")
  String serviceName;//decrypt.privateKey

  @Value("${decrypt.privateKey}")
  String privateKey;

  public AuthenticationService(UserRepository userRepository,
                               UserService userService, CompanyRepository companyRepository,
                               GroupRepository groupRepository,
                               UserGroupRepository userGroupRepository,
                               UserGroupPermissionRepository userGroupPermissionRepository,
                               UserLoginRepository userLoginRepository,
                               ApplicationAccountRepository applicationAccountRepository,
                               ModelMapper modelMapper,
                               MessageService messageService,
                               SystemSettingService systemSettingService,
                               NotificationService notificationService,
                               SystemSettingService systemSettingService1,
                               PasswordEncoder passwordEncoder,
                               JWTTokens jwtTokens,
                               ApplicationService applicationService,
                               UserGroupService userGroupService,
                               NotificationCacheService notificationCacheService) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.companyRepository = companyRepository;
    this.groupRepository = groupRepository;
    this.userGroupRepository = userGroupRepository;
    this.userGroupPermissionRepository = userGroupPermissionRepository;
    this.userLoginRepository = userLoginRepository;
    this.applicationAccountRepository = applicationAccountRepository;
    this.modelMapper = modelMapper;
    this.messageService = messageService;
    this.notificationService = notificationService;
    this.systemSettingService = systemSettingService1;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokens = jwtTokens;
    this.applicationService = applicationService;
    this.userGroupService = userGroupService;
    this.notificationCacheService = notificationCacheService;
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public UserRegisterResponse registerUser(UserRegisterRequest dto) {

    try {
      dto.setPassword(GenericRSAUtil.decryptWithPrivateKey(dto.getPassword(), privateKey));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    User existing  = userRepository.findByEmail(dto.getEmail());
    if (existing != null) {
      throw new AppException(messageService.getMessage("User.Email.Exist"));
    }

    existing = userRepository.findByPhone(dto.getPhone());
    if (existing != null) {
      throw new AppException(messageService.getMessage("User.Phone.Exist"));
    }

    Company company = companyRepository.findByClientId(RequestUtil.getClientId());
    if (company == null) {
      throw new AppException(messageService.getMessage("Company.NotFound"));
    }

    User newUser = User.builder()
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .companyCode(company.getCompanyCode())
            .isEmailVerified(false)
            .isPhoneVerified(false)
            .userType(GroupType.Customer.name())
            .lang(Locale.getDefault().getLanguage())
            .status(EntityStatus.Enabled.name())
            .createdBy(GroupType.Customer.name())
            .createdAt(new Date())
            .build();

    newUser.setSkipAudit(true);
    newUser.setSkipAuthorization(true);
    User newEntry = userRepository.save(newUser);

    userLoginRepository.save(UserLogin.builder()
            .password(passwordEncoder.encode(dto.getPassword()))
            .status(EntityStatus.Disabled.name())//Until phone and email is verified
            .createdAt(new Date())
            .userId(newEntry.getId())
            .companyCode(newEntry.getCompanyCode())
            .uuid(UUID.randomUUID().toString())
            .build());

    String groupName = GroupType.Customer.name();
    Group group = groupRepository.findByGroupNameAndCompanyCode(groupName,
        newEntry.getCompanyCode());

    if (group == null) {
      throw new AppException(messageService.getMessage("Group.NotFound").replace("{0}", groupName));
    }

    UserGroup userGroup =  new UserGroup();
    userGroup.setUserId(newEntry.getId());
    userGroup.setGroupId(group.getId());
    userGroup.setCompanyCode(company.getCompanyCode());
    userGroupRepository.save(userGroup);

    //Send otp to verify email via Notification Service
    notificationService.sendNotification(NotificationRequest.builder()
            .requiredValidation(true)
            .validationType(NotificationType.EmailVerify)
            .recipientEmail(newEntry.getEmail())
            .senderEmail(senderMail)
            .message(messageService.getMessage("message.email.verify"))
            .companyCode(newEntry.getCompanyCode())
            .subject(messageService.getMessage("email.verify.subject"))
            .recipientPhone(null)
            .build(), AppUtil.generateOtp());

    //Send otp to verify phone via Notification Service
    notificationService.sendNotification(NotificationRequest.builder()
            .requiredValidation(true)
            .validationType(NotificationType.PhoneVerify)
            .senderPhone(senderSms)
            .senderEmail(null)
            .message(messageService.getMessage("message.phone.verify"))
            .companyCode(newEntry.getCompanyCode())
            .subject(messageService.getMessage("phone.verify.subject"))
            .recipientPhone(newEntry.getPhone())
            .build(), AppUtil.generateOtp());

    UserRegisterResponse response = new UserRegisterResponse();
    response.setResponseMessage(messageService.getMessage("User.Register.Success"));
    return response;
  }

  public UserLoginResponse login(UserLoginRequest dto) {
    Company company = companyRepository.findByClientId(RequestUtil.getClientId());
    if (company == null) {
      throw new AppException(messageService.getMessage("Company.NotFound"));
    }

    dto.setPassword(GenericRSAUtil.decryptWithPrivateKey(dto.getPassword(), privateKey));

    if (EntityStatus.Disabled.name().equalsIgnoreCase(company.getStatus())) {
      throw new AppException(messageService.getMessage("Company.Account.Disabled"));
    }

    User user = userRepository.findByCompanyCodeAndEmailOrPhone(company.getCompanyCode(), dto.getUsername());
    if (user == null) {
      log.info("User not found for company:{}, username:{}", company.getCompanyCode(), dto.getUsername());
      throw new AppException(messageService.getMessage("Login.Failed.NotFound"));
    }

    if (EntityStatus.Deleted.name().equalsIgnoreCase(user.getStatus())) {
      throw new AppException(messageService.getMessage("User.Account.Deleted"));
    }

    if (user.getLoginAttemptCount() == null) {
      user.setLoginAttemptCount(0);
    }

    if (RequestUtil.getChannel().equalsIgnoreCase(RequestSource.PlatformBackend.getValue()) && GroupType.Customer.name().equalsIgnoreCase(user.getUserType())) {
      throw new AppException(messageService.getMessage("UserType.NotAllowed"));
    }

    UserLogin userLogin = userLoginRepository.findByUserId(user.getId());
    if (userLogin == null) {
      log.info("UserLogin not found for userId:{}", user.getId());
      throw new AppException(messageService.getMessage("Login.Failed"));
    }

    if (EntityStatus.Disabled.name().equalsIgnoreCase(user.getStatus())) {
      throw new AppException(messageService.getMessage("User.Account.Disabled"));
    }

    int maxLoginAttempts = 3;
    if (!passwordEncoder.matches(dto.getPassword(), userLogin.getPassword())) {
      int loginAttemptLeft = maxLoginAttempts - (user.getLoginAttemptCount() +  1);
      log.info("UserLogin password does not match for email: {}", user.getEmail());

      if (user.getLoginAttemptCount() < maxLoginAttempts) {
        user.setLoginAttemptCount(user.getLoginAttemptCount() + 1);
      }

      if (user.getLoginAttemptCount() >= maxLoginAttempts) {
        user.setStatus(EntityStatus.Disabled.name());
      }

      user.setSkipAudit(true);
      user.setSkipAuthorization(true);
      userRepository.save(user);

      if (loginAttemptLeft <= 0) {
        throw new AppException(messageService.getMessage("Login.Failed.Block"));
      }

      throw new AppException(messageService.getMessage("Login.Failed").replace("{0}", String.valueOf(loginAttemptLeft)));
    }

    if (user.getLoginAttemptCount() > 0) {
      user.setLoginAttemptCount(0);
      user.setSkipAudit(true);
      user.setSkipAuthorization(true);
      userRepository.save(user);
    }

    List<String> audience = applicationService.getAudience(user.getId());

    AuthToken authToken = modelMapper.map(user, AuthToken.class);
    String accessToken = jwtTokens.generateAccessToken(company, dto.getUsername());
    String refreshToken = jwtTokens.generateRefreshToken(company, dto.getUsername());
    authToken.setAccessToken(accessToken);
    authToken.setAudience(audience);
    authToken.setCompanyName(company.getCompanyName());
    authToken.setUsername(dto.getUsername());
    authToken.setUuid(userLogin.getUuid());
    authToken.setRefreshToken(refreshToken);
    authToken.setEmailVerified(user.isEmailVerified());
    authToken.setPhoneVerified(user.isPhoneVerified());
    authToken.setHasChangedPassword(user.getHasChangedPassword() != null && user.getHasChangedPassword());
    authToken.setExpires(jwtTokens.parseJWT(accessToken).getExpiration().toString());
    authToken.setUserType(user.getUserType());

    userLogin.setAccessToken(accessToken);
    userLogin.setRefreshToken(refreshToken);
    userLoginRepository.save(userLogin);

    RequestUtil.setApprovalMessage(messageService.getMessage("login.success"));
    return UserLoginResponse.builder()
            .data(authToken)
            .build();
  }

  public UserLogoutResponse logout() {
    String token = RequestUtil.getToken();
    Claims claims = jwtTokens.parseJWT(token);
    String companyCode = (String) claims.get("issuer");
    Company company = companyRepository.findByCompanyCode(companyCode);
    String username = (String) claims.get("subject");

    User user = userRepository.findByCompanyCodeAndEmailOrPhone(company.getCompanyCode(), username);
    if (user == null) {
      log.info("User not found for company:{}, username:{}", company.getCompanyCode(), username);
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    UserLogin userLogin = userLoginRepository.findByUserIdAndAccessToken(user.getId(), token);
    if (userLogin == null) {
      log.info("UserLogin not found for userId:{}", user.getId());
      throw new AppException(messageService.getMessage("Token.Not.Found"));
    }

    userLogin.setAccessToken("");
    userLogin.setRefreshToken("");

    userLoginRepository.save(userLogin);
    return new UserLogoutResponse();
  }

  public AccessTokenResponse getAccessTokenFromRefreshToken(AccessTokenRequest dto) {
    Claims claims = jwtTokens.parseRefreshJWT(dto.getRefreshToken());

    log.info("Claims: {}", new Gson().toJson(claims));

    String companyCode = (String) claims.get("issuer");
    Company company = companyRepository.findByCompanyCode(companyCode);
    String username =  (String) claims.get("subject");

    User user = userRepository.findByCompanyCodeAndEmailOrPhone(company.getCompanyCode(), username);
    if (user == null) {
      log.info("User not found for company:{}, username:{}", company.getCompanyCode(), username);
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    UserLogin userLogin = userLoginRepository.findByUserId(user.getId());
    if (userLogin == null) {
      log.info("UserLogin not found for userId:{}", user.getId());
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    if (!dto.getRefreshToken().equals(userLogin.getRefreshToken())) {
      log.info("Refresh token not match for userId:{}", username);
      throw new AppException(messageService.getMessage("Token.Fraud"));
    }

    List<String> audience = applicationService.getAudience(user.getId());

    AuthToken authToken = modelMapper.map(user, AuthToken.class);
    String accessToken = jwtTokens.generateAccessToken(company, username);
    String refreshToken = jwtTokens.generateRefreshToken(company, username);
    authToken.setAccessToken(accessToken);
    authToken.setAudience(audience);
    authToken.setUsername(username);
    authToken.setRefreshToken(refreshToken);
    authToken.setCompanyName(company.getCompanyName());
    authToken.setUuid(userLogin.getUuid());
    authToken.setLang(user.getLang());
    authToken.setExpires(jwtTokens.parseJWT(accessToken).getExpiration().toString());

    userLogin.setAccessToken(accessToken);
    userLogin.setRefreshToken(refreshToken);
    userLoginRepository.save(userLogin);

    return AccessTokenResponse.builder()
            .data(authToken)
            .build();

  }

  private boolean isAuthenticated(String token, User user) {
    Long userId  = user.getId();
    UserLogin userLogin = userLoginRepository.findByUserIdAndAccessToken(userId, token);
    if (userLogin == null) {
      throw new AppException(messageService.getMessage("Token.Not.Found"));
    }

    if (!token.equals(userLogin.getAccessToken())) {
      log.info("Token not match for userId:{}", user.getId());
      throw new AppException(messageService.getMessage("Token.Fraud"));
    }

    return true;
  }

  /**
   *
   * @param endpoint is a hash value of SHA 256 of appName,permission e.g authorization-service,VERIFY_EMAIL_OTP
   * @return true if the user has the permission to call the endpoint, otherwise return false
   */
  public boolean isAuthorized(String endpoint, User user) {
    if (GroupType.SuperAdmin.name().equals(user.getUserType()) || GroupType.Admin.name().equals(user.getUserType())) {
      return true;
    }

    //Allow access to common endpoints
    if (PermissionUtil.COMMON_DEFAULT_PERMISSIONS.contains(endpoint)) {
      return true;
    }

    if (GroupType.Customer.name().equals(user.getUserType()) && PermissionUtil.getCustomerPermissionMap().containsKey(endpoint)) {
      return true;
    }

    Long userId  = user.getId();

    //Check for path variable as the permissionId hashed might fail
    ApplicationPermission resource = applicationService.getByPermissionId(endpoint);

    if (resource == null) {
      throw new AppException(messageService.getMessage("App.Permission.NotFound"));
    }

    if (!resource.isSecured()) {
      return true;
    }

    List<ApplicationAccount> userPermissions = applicationAccountRepository.findByUserId(userId);
    for (ApplicationAccount userPermission : userPermissions) {
      if (resource.getPermissionId().equalsIgnoreCase(userPermission.getPermissionId())) {
        return true;
      }
    }

    List<Long> groupIds = userGroupService.groupIds(userId);
    List<UserGroupPermission> groupPermissions = userGroupPermissionRepository.findByGroupIdIn(groupIds);
    List<Long> applicationPermissionIds = new ArrayList<>();
    for (UserGroupPermission groupPermission : groupPermissions) {
      applicationPermissionIds.add(groupPermission.getApplicationPermissionId());
    }

    return applicationPermissionIds.contains(resource.getId());
  }

  public TokenVerifyResponse verifyToken(TokenVerifyRequest request) {

    TokenVerifyResponse response = new TokenVerifyResponse();
    response.setResponseCode(ResponseCode.UN_AUTHORIZED.getValue());
    response.setResponseMessage(messageService.getMessage("Auth.UnAuthorized"));

    String companyClientId = RequestUtil.getClientId();
    Company company = companyRepository.findByClientId(companyClientId);
    if (company == null) {
      throw new AppException(ResponseCode.UN_AUTHORIZED.getValue(), messageService.getMessage("Company.NotFound"));
    }

    SystemSetting systemSetting = RequestUtil.getSettings().get(SystemSettingKeys.PLATFORM_STATE.getValue());

    User user = userRepository.findByUsername(request.getUsername());
    boolean superAdmin;
    if (RequestSource.Service.name().equalsIgnoreCase(request.getRequestSource())) {
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

    SystemSetting systemSetting1 = RequestUtil.getSettings().get(SystemSettingKeys.BILLS_PAYMENT_SERVICE_STATE.getValue());
    if (!PlatformState.Active.name().equalsIgnoreCase(systemSetting1.getValue()) && RequestUtil.getPermissionMaps().get(SystemSettingKeys.BILLS_PAYMENT_SERVICE_STATE.getValue()).contains(request.getPermissionId())) {
      throw new AppException(messageService.getMessage("Bills.State.Maintenance"));
    }

    systemSetting1 = RequestUtil.getSettings().get(SystemSettingKeys.TRANSFER_SERVICE_STATE.getValue());
    if (!PlatformState.Active.name().equalsIgnoreCase(systemSetting1.getValue()) && RequestUtil.getPermissionMaps().get(SystemSettingKeys.TRANSFER_SERVICE_STATE.getValue()).contains(request.getPermissionId())) {
      throw new AppException(messageService.getMessage("Transfer.State.Maintenance"));
    }

    systemSetting1 = RequestUtil.getSettings().get(SystemSettingKeys.LOAN_SERVICE_STATE.getValue());
    if (!PlatformState.Active.name().equalsIgnoreCase(systemSetting1.getValue()) && RequestUtil.getPermissionMaps().get(SystemSettingKeys.LOAN_SERVICE_STATE.getValue()).contains(request.getPermissionId())) {
      throw new AppException(messageService.getMessage("Loans.State.Maintenance"));
    }

    if (EntityStatus.Disabled.name().equals(company.getStatus()) && !superAdmin) {
      throw new AppException(messageService.getMessage("Company.Account.Disabled"));
    }

    //Client id of the service on application table
    String serviceClientId = request.getServiceClientId();
    Application application = applicationService.getByPublicKey(serviceClientId);
    if (application == null) {
      throw new AppException(messageService.getMessage("App.Setup.NotFound"));
    }

    if (EntityStatus.Shutdown.name().equals(application.getStatus()) && !superAdmin) {
      throw new AppException(messageService.getMessage("Application.Shutdown"));
    }

    if (EntityStatus.Disabled.name().equals(application.getStatus()) && !superAdmin) {
      throw new AppException(messageService.getMessage("Application.Disabled"));
    }

    if (!request.isSecured()) {
      response.setResponseCode(ResponseCode.SUCCESSFUL.getValue());
      response.setResponseMessage(messageService.getMessage("Auth.Successful"));
      response.setData(TokenVerifyResponse.Data.builder()
              .companyCode(company.getCompanyCode())
              .build());
      return response;
    }

    //Token forwarded by API Gateway or frontend client
    String userToken = request.getToken();

    if (userToken == null || userToken.trim().isEmpty()) {
      throw new AppException(ResponseCode.UN_AUTHORIZED.getValue(), messageService.getMessage("Authentication.Required"));
    }
    userToken = userToken.replace("Bearer", "").trim();

    Claims claims = jwtTokens.parseJWT(userToken);
    String username = (String) claims.get("subject");

    user = userRepository.findByCompanyCodeAndEmailOrPhone(company.getCompanyCode(), username);

    if (user == null) {
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    RequestUtil.setUserType(user.getUserType());

    if (request.getUsername() != null && !request.getUsername().trim().equalsIgnoreCase("") && !username.trim().equalsIgnoreCase(request.getUsername())) {
      throw new AppException(messageService.getMessage("Usernames.Mismatch"));
    }

    if (RequestSource.PlatformBackend.getValue().equalsIgnoreCase(request.getRequestSource())
    && GroupType.Customer.name().equals(user.getUserType())) {
      throw new AppException(messageService.getMessage("UserType.NotAllowed"));
    }

    if (RequestSource.Mobile.getValue().equalsIgnoreCase(request.getRequestSource()) && !GroupType.Customer.name().equalsIgnoreCase(user.getUserType())) {
      throw new AppException(messageService.getMessage("UserType.NotAllowed"));
    }

    if (EntityStatus.Disabled.name().equals(user.getStatus())) {
      throw new AppException(ResponseCode.UN_AUTHORIZED.getValue(), messageService.getMessage("User.Account.Disabled"));
    }

    UserLogin userLogin = userLoginRepository.findByCompanyCodeAndUserId(company.getCompanyCode(), user.getId());
    if (userLogin == null) {
      throw new AppException(messageService.getMessage("Company.NotFound"));
    }

    String changePasswordPermissionId = HashUtil.getHash(serviceName + "CHANGE_PASSWORD");
    if (GroupType.Staff.name().equals(user.getUserType()) && !user.getHasChangedPassword() && !request.getPermissionId().equalsIgnoreCase(changePasswordPermissionId)) {
      throw new AppException(messageService.getMessage("Password.Change.Required"));
    }

    String endpoint = request.getPermissionId();

    RequestUtil.getAuthToken().setUsername(username);

    if (isAuthenticated(userToken, user)) {
      if (isAuthorized(endpoint, user)) {
        response.setResponseCode(ResponseCode.SUCCESSFUL.getValue());
        response.setResponseMessage(messageService.getMessage("Auth.Successful"));
      } else {
        response.setResponseCode(ResponseCode.FORBIDDEN.getValue());
        response.setResponseMessage(messageService.getMessage("Auth.Forbidden"));
      }
    } else {
      response.setResponseCode(ResponseCode.UN_AUTHORIZED.getValue());
      response.setResponseMessage(messageService.getMessage("Auth.UnAuthorized"));
    }

    response.setData(TokenVerifyResponse.Data.builder()
                    .loginId(userLogin.getUuid())
                    .companyCode(userLogin.getCompanyCode())
                    .username(username)
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .lang(user.getLang())
                    .status(userLogin.getStatus())
                    .isPhoneVerified(user.isPhoneVerified())
                    .isEmailVerified(user.isEmailVerified())
                    .userType(user.getUserType())
                    .hasChangedPassword(user.getHasChangedPassword() != null && user.getHasChangedPassword())
                    .build());

    return response;
  }

  public OtpVerifyResponse verifyOtp(String otp, NotificationType notificationType) {
    String companyCode = RequestUtil.getAuthToken().getCompanyCode();
    String username = RequestUtil.getAuthToken().getUsername();
    User user = userRepository.findByCompanyCodeAndEmailOrPhone(companyCode, username);
    if (user == null) {
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    boolean isValidOtp = notificationCacheService.isValid(companyCode, user.getId(), notificationType, otp);
    OtpVerifyResponse response = new OtpVerifyResponse();
    if (isValidOtp) {
      response.setResponseCode(ResponseCode.SUCCESSFUL.getValue());
      response.setResponseMessage(messageService.getMessage("Otp.Verify.Success"));

      if (NotificationType.EmailVerify.equals(notificationType)) {
        user.setEmailVerified(true);
      }

      if (NotificationType.PhoneVerify.equals(notificationType)) {
        user.setPhoneVerified(true);
      }

      user.setSkipAudit(true);
      user.setSkipAuthorization(true);
      userRepository.save(user);

      notificationCacheService.deleteUsedOtp(companyCode, user.getId(), notificationType, otp);

      return response;
    }

    throw new AppException(messageService.getMessage("Otp.Verify.Fail"));
  }

  public OtpResendResponse resendPhoneOtp() {
    notificationService.sendNotification(NotificationRequest.builder()
            .requiredValidation(true)
            .validationType(NotificationType.PhoneVerify)
            .senderPhone(senderSms)
            .senderEmail(null)
            .message(messageService.getMessage("message.phone.verify"))
            .companyCode(RequestUtil.getAuthToken().getCompanyCode())
            .subject(messageService.getMessage("phone.verify.subject"))
            .recipientPhone(RequestUtil.getAuthToken().getPhone())
            .build(), AppUtil.generateOtp());
    OtpResendResponse resp = new OtpResendResponse();
    resp.setResponseCode(ResponseCode.SUCCESSFUL.getValue());
    resp.setResponseMessage(messageService.getMessage("Message.Successful"));
    return resp;
  }

  public OtpResendResponse resendEmailOtp() {
    notificationService.sendNotification(NotificationRequest.builder()
                    .requiredValidation(true)
                    .validationType(NotificationType.EmailVerify)
                    .senderEmail(senderMail)
                    .senderPhone(null)
                    .message(messageService.getMessage("message.email.verify"))
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .subject(messageService.getMessage("email.verify.subject"))
                    .recipientEmail(RequestUtil.getAuthToken().getEmail())
                    .build(), AppUtil.generateOtp());
    OtpResendResponse resp = new OtpResendResponse();
    resp.setResponseCode(ResponseCode.SUCCESSFUL.getValue());
    resp.setResponseMessage(messageService.getMessage("Message.Successful"));
    return resp;
  }

  public OtpVerifyResponse verifyEmailOtp(String otp) {
    return verifyOtp(otp, NotificationType.EmailVerify);
  }

  public OtpVerifyResponse verifyPhoneOtp(String otp) {
    return verifyOtp(otp, NotificationType.PhoneVerify);
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public ResetPasswordResponse resetPassword(ResetPasswordRequest dto) {

    dto.setNewPassword(GenericRSAUtil.decryptWithPrivateKey(dto.getNewPassword(), privateKey));
    dto.setConfirmPassword(GenericRSAUtil.decryptWithPrivateKey(dto.getConfirmPassword(), privateKey));

    User user = userRepository.findByUsername(dto.getUsername());
    if (user == null) {
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    UserLogin userLogin = userLoginRepository.findByUserId(user.getId());
    if (userLogin == null) {
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    boolean isValid = notificationCacheService.isValid(userLogin.getCompanyCode(), userLogin.getUserId(), NotificationType.ResetPasswordVerify, dto.getOtp());

    if (!isValid) {
      throw new AppException(messageService.getMessage("Otp.Verify.Fail"));
    }

    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
      throw new AppException(messageService.getMessage("ConfirmPassword.Mismatch"));
    }

    userLogin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    userLoginRepository.save(userLogin);
    notificationCacheService.deleteUsedOtp(userLogin.getCompanyCode(), user.getId(), NotificationType.ResetPasswordVerify, dto.getOtp());
    return new ResetPasswordResponse();
  }

  public ResetPasswordInitiateResponse resetPasswordInitiate(String username) {
    User user = userRepository.findByUsername(username);

    if (user == null) {
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    String otp = AppUtil.generateOtp();

    notificationService.sendNotification(NotificationRequest.builder()
            .requiredValidation(true)
            .validationType(NotificationType.ResetPasswordVerify)
            .senderEmail(senderMail)
            .message(messageService.getMessage("message.email.reset-password"))
            .companyCode(user.getCompanyCode())
            .subject(messageService.getMessage("email.reset-password.subject"))
            .recipientEmail(user.getEmail())
            .build(), otp);

    notificationService.sendNotification(NotificationRequest.builder()
            .requiredValidation(true)
            .validationType(NotificationType.ResetPasswordVerify)
            .senderPhone(senderSms)
            .message(messageService.getMessage("message.phone.reset-password"))
            .companyCode(user.getCompanyCode())
            .subject(messageService.getMessage("phone.reset-password.subject"))
            .recipientPhone(user.getPhone())
            .build(), otp);

    ResetPasswordInitiateResponse response = ResetPasswordInitiateResponse.builder()
            .build();
    response.setResponseCode(ResponseCode.SUCCESSFUL.getValue());
    response.setResponseMessage(messageService.getMessage("Message.Successful"));
    response.setData(ResetPasswordInitiateResponse.Data.builder()
                    .otpHash(HashUtil.getHash(otp))
                    .build());
    return response;
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public ChangePasswordResponse changePassword(ChangePasswordRequest dto) {
    dto.setCurrentPassword(GenericRSAUtil.decryptWithPrivateKey(dto.getCurrentPassword(), privateKey));
    dto.setNewPassword(GenericRSAUtil.decryptWithPrivateKey(dto.getNewPassword(), privateKey));
    dto.setConfirmPassword(GenericRSAUtil.decryptWithPrivateKey(dto.getConfirmPassword(), privateKey));

    String loginId = RequestUtil.getAuthToken().getUuid();
    UserLogin userLogin = userLoginRepository.findByUuid(loginId);
    if (userLogin == null) {
      throw new AppException(messageService.getMessage("invalid.login"));
    }

    if (!passwordEncoder.matches(dto.getCurrentPassword(), userLogin.getPassword())) {
      throw new AppException(messageService.getMessage("Password.Mismatch"));
    }

    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
      throw new AppException(messageService.getMessage("ConfirmPassword.Mismatch"));
    }

    userLogin.setPassword(passwordEncoder.encode(dto.getNewPassword()));

    userLoginRepository.save(userLogin);

    User user = userRepository.findById(userLogin.getUserId()).orElse(null);
    if (user == null) {
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    user.setHasChangedPassword(true);
    user.setSkipAudit(true);
    user.setSkipAuthorization(true);
    userRepository.save(user);

    return new ChangePasswordResponse();
  }

  public UpdateUserResponse blockCustomer(String loginId) {
    UserLogin existing = userLoginRepository.findByUuid(loginId);

    if (existing == null) {
      throw new AppException(messageService.getMessage("User.NotFound"));
    }

    User user = userRepository.findById(existing.getUserId()).orElse(null);

    if (user == null) {
      throw new AppException(messageService.getMessage("User.NotFound"));
    }

    user.setUpdatedAt(new Date());
    user.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
    user.setStatus(EntityStatus.Disabled.name());
    user.setSkipAudit(true);
    user.setSkipAuthorization(true);
    userRepository.save(user);
    UpdateUserResponse response = new UpdateUserResponse();
    response.setData(modelMapper.map(user, UpdateUserResponse.Data.class));
    return response;
  }

  public UpdateUserResponse unblockCustomer(String loginId) {
    UserLogin existing = userLoginRepository.findByUuid(loginId);

    if (existing == null) {
      throw new AppException(messageService.getMessage("User.NotFound"));
    }

    User user = userRepository.findById(existing.getUserId()).orElse(null);

    if (user == null) {
      throw new AppException(messageService.getMessage("User.NotFound"));
    }

    user.setUpdatedAt(new Date());
    user.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
    user.setStatus(EntityStatus.Enabled.name());
    user.setSkipAudit(true);
    user.setSkipAuthorization(true);
    userRepository.save(user);
    UpdateUserResponse response = new UpdateUserResponse();
    response.setData(modelMapper.map(user, UpdateUserResponse.Data.class));
    return response;
  }

  public InitiateOtpResponse deleteUserProfileInitiate() {

    User user = userRepository.findByUsername(RequestUtil.getAuthToken().getUsername());

    if (user == null) {
      throw new AppException(messageService.getMessage("User.Not.Found"));
    }

    String otp = AppUtil.generateOtp();

    notificationService.sendNotification(NotificationRequest.builder()
            .requiredValidation(true)
            .validationType(NotificationType.DeleteProfileVerify)
            .senderEmail(senderMail)
            .message(messageService.getMessage("message.delete-profile"))
            .companyCode(user.getCompanyCode())
            .subject(messageService.getMessage("subject.delete-profile"))
            .recipientEmail(user.getEmail())
            .build(), otp);

    notificationService.sendNotification(NotificationRequest.builder()
            .requiredValidation(true)
            .validationType(NotificationType.DeleteProfileVerify)
            .senderPhone(senderSms)
            .message(messageService.getMessage("message.delete-profile"))
            .companyCode(user.getCompanyCode())
            .subject(messageService.getMessage("subject.delete-profile"))
            .recipientPhone(user.getPhone())
            .build(), otp);

    InitiateOtpResponse response = InitiateOtpResponse.builder()
            .build();
    response.setResponseCode(ResponseCode.SUCCESSFUL.getValue());
    response.setResponseMessage(messageService.getMessage("Message.Successful"));
    response.setData(InitiateOtpResponse.Data.builder()
            .otpHash(HashUtil.getHash(otp))
            .build());
    return response;

  }

  public DeleteUserResponse deleteUserProfile(String otp) {
    String loginId = RequestUtil.getAuthToken().getUuid();
    UserLogin existing = userLoginRepository.findByUuid(loginId);

    if (existing == null) {
      throw new AppException(messageService.getMessage("User.NotFound"));
    }

    User user = userRepository.findById(existing.getUserId()).orElse(null);

    if (user == null) {
      throw new AppException(messageService.getMessage("User.NotFound"));
    }

    boolean isValid = notificationCacheService.isValid(existing.getCompanyCode(), existing.getUserId(), NotificationType.DeleteProfileVerify, otp);

    if (!isValid) {
      throw new AppException(messageService.getMessage("Otp.Verify.Fail"));
    }

    user.setUpdatedAt(new Date());
    user.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
    user.setStatus(EntityStatus.Deleted.name());
    user.setSkipAudit(true);
    user.setSkipAuthorization(true);
    userRepository.save(user);

    existing.setAccessToken("");
    existing.setRefreshToken("");
    userLoginRepository.save(existing);

    DeleteUserResponse response = new DeleteUserResponse();
    response.setData(modelMapper.map(user, DeleteUserResponse.Data.class));
    response.setResponseMessage("User profile deleted successfully");
    return response;
  }
}
