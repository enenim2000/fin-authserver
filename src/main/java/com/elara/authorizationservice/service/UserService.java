package com.elara.authorizationservice.service;

import com.elara.authorizationservice.auth.RequestUtil;
import com.elara.authorizationservice.domain.Company;
import com.elara.authorizationservice.domain.User;
import com.elara.authorizationservice.domain.UserLogin;
import com.elara.authorizationservice.dto.request.CreateUserRequest;
import com.elara.authorizationservice.dto.request.NotificationRequest;
import com.elara.authorizationservice.dto.request.SearchStaffRequest;
import com.elara.authorizationservice.dto.request.UpdateUserRequest;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.enums.EntityStatus;
import com.elara.authorizationservice.enums.GroupType;
import com.elara.authorizationservice.enums.Language;
import com.elara.authorizationservice.exception.AppException;
import com.elara.authorizationservice.repository.CompanyRepository;
import com.elara.authorizationservice.repository.SystemSettingRepository;
import com.elara.authorizationservice.repository.UserLoginRepository;
import com.elara.authorizationservice.repository.UserRepository;
import com.elara.authorizationservice.util.JsonConverter;
import com.elara.authorizationservice.util.PaginationUtil;
import com.elara.authorizationservice.util.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class UserService {

    final UserRepository userRepository;
    final UserLoginRepository userLoginRepository;
    final CompanyRepository companyRepository;
    final ModelMapper modelMapper;
    final MessageService messageService;
    final NotificationService notificationService;
    final PasswordEncoder passwordEncoder;

    final SystemSettingRepository systemSettingRepository;

    @Value("${app.mail.sender}")
    String senderMail;

    @Value("${default.admin.email}")
    private String defaultAdminEmail;

    @Value("${default.admin.phone}")
    private String defaultAdminPhone;

    @Value("${default.companyCode}")
    private String defaultCompanyCode;


    public UserService(UserRepository userRepository,
                       UserLoginRepository userLoginRepository,
                       CompanyRepository companyRepository,
                       ModelMapper modelMapper,
                       MessageService messageService, NotificationService notificationService,
                       PasswordEncoder passwordEncoder, SystemSettingRepository systemSettingRepository) {
        this.userRepository = userRepository;
        this.userLoginRepository = userLoginRepository;
        this.companyRepository = companyRepository;
        this.modelMapper = modelMapper;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CreateUserResponse createUser(CreateUserRequest dto) {

        Company company = companyRepository.findByCompanyCode(dto.getCompanyCode());
        if (company == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        User existing = userRepository.findByUsername(dto.getEmail());
        if (existing != null) {
            throw new AppException(messageService.getMessage("User.Exist"));
        }

        existing = userRepository.findByUsername(dto.getPhone());
        if (existing != null) {
            throw new AppException(messageService.getMessage("User.Exist"));
        }

        if (dto.getLang() != null && !Language.isValid(dto.getLang())) {
            throw new AppException(messageService.getMessage("User.Language.NotSupported").replace("{0}", dto.getLang()));
        }

        if (dto.getUserType() == null || !GroupType.isValid(dto.getUserType())) {
            throw new AppException(messageService.getMessage("UserType.Invalid"));
        }

        User loggedInUser = userRepository.findByUsername(RequestUtil.getAuthToken().getUsername());

        if (GroupType.Staff.name().equals(loggedInUser.getUserType()) && (GroupType.Admin.name().equals(dto.getUserType()) || GroupType.SuperAdmin.name().equals(dto.getUserType()))) {
            throw new AppException(messageService.getMessage("Staff.Admin.NotAllowed"));
        }

        if (GroupType.Admin.name().equals(loggedInUser.getUserType()) && (GroupType.Admin.name().equals(dto.getUserType()) || GroupType.SuperAdmin.name().equals(dto.getUserType()))) {
            throw new AppException(messageService.getMessage("Admin.Admin.NotAllowed"));
        }

        String password = generatePassword();
        String subject = messageService.getMessage("message.change.default-password-subject");
        String message = messageService.getMessage("message.change.default-password").replace("{0}", password);

        User newEntry = modelMapper.map(dto, User.class);
        newEntry.setCreatedBy(RequestUtil.getAuthToken().getUsername());
        newEntry.setCreatedAt(new Date());

        newEntry.setUserType(dto.getUserType());
        newEntry.setCompanyCode(company.getCompanyCode());
        newEntry.setStatus(EntityStatus.Enabled.name());
        newEntry.setHasChangedPassword(false);

        if (GroupType.SuperAdmin.name().equals(RequestUtil.getUserType())) {
            newEntry.setSkipAudit(true);
            newEntry.setSkipAuthorization(true);
        } else {
            newEntry.setSkipAudit(false);
            newEntry.setSkipAuthorization(false);
            newEntry.setResponseType(AuthorizationUserResponse.class.getName());
            newEntry.addApprovalMail(ApprovalMail.builder().subject(subject).email(newEntry.getEmail()).message(message).build());
            newEntry.addApprovalDependency(ApprovalDependency.builder().className(UserLogin.class.getSimpleName()).fieldName("userId").build());
        }
        newEntry = userRepository.save(newEntry);

        log.info("info: {}", password);
        userLoginRepository.save(UserLogin.builder()
                .password(passwordEncoder.encode(password))
                .status(EntityStatus.Enabled.name())
                .createdAt(new Date())
                .userId(newEntry.getId())
                .companyCode(newEntry.getCompanyCode())
                .uuid(UUID.randomUUID().toString())
                .build());

        if (newEntry.getSkipAuthorization()) {
            notificationService.sendEmail(NotificationRequest.builder()
                    .message(message)
                    .html(message)
                    .companyCode(company.getCompanyCode())
                    .recipientEmail(newEntry.getEmail())
                    .senderEmail(senderMail)
                    .requiredValidation(false)
                    .subject(subject)
                    .build());
        }

        CreateUserResponse response = new CreateUserResponse();
        response.setData(modelMapper.map(newEntry, CreateUserResponse.Data.class));
        return response;
    }

    public UpdateUserResponse updateUser(UpdateUserRequest dto) {
        User existing = userRepository.findByUsername(dto.getEmail());
        if (existing == null) {
            throw new AppException(messageService.getMessage("User.NotFound"));
        }

        if (dto.getUserType() == null || !GroupType.isValid(dto.getUserType())) {
            throw new AppException(messageService.getMessage("UserType.Invalid"));
        }

        User loggedInUser = userRepository.findByUsername(RequestUtil.getAuthToken().getUsername());

        if (GroupType.Staff.name().equals(loggedInUser.getUserType()) && (GroupType.Admin.name().equals(dto.getUserType()) || GroupType.SuperAdmin.name().equals(dto.getUserType()))) {
            throw new AppException(messageService.getMessage("Staff.Admin.NotAllowed"));
        }

        if (GroupType.Admin.name().equals(loggedInUser.getUserType()) && (GroupType.Admin.name().equals(dto.getUserType()) || GroupType.SuperAdmin.name().equals(dto.getUserType()))) {
            throw new AppException(messageService.getMessage("Admin.Admin.NotAllowed"));
        }

        existing.setBefore(JsonConverter.getJsonRecursive(existing));//Needed for approval process

        modelMapper.map(dto, existing);
        if (GroupType.SuperAdmin.name().equals(RequestUtil.getUserType())) {
            existing.setSkipAudit(true);
            existing.setSkipAuthorization(true);
        } else {
            existing.setSkipAudit(false);
            existing.setSkipAuthorization(false);
            existing.setResponseType(AuthorizationUserResponse.class.getName());
        }

        existing.setUpdatedAt(new Date());
        existing.setUserType(dto.getUserType());
        existing.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
        existing = userRepository.save(existing);
        UpdateUserResponse response = new UpdateUserResponse();
        response.setData(modelMapper.map(existing, UpdateUserResponse.Data.class));
        return response;
    }

    private String generatePassword() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return null;//ERROR_CODE;
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };

        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        return gen.generatePassword(10, splCharRule, lowerCaseRule,
                upperCaseRule, digitRule);
    }

    public UpdateUserResponse toggleUser(Long id) {
        User existing = userRepository.findById(id).orElse(null);

        if (existing == null) {
            throw new AppException(messageService.getMessage("User.NotFound"));
        }

        if (EntityStatus.Enabled.name().equalsIgnoreCase(existing.getStatus())) {
            existing.setStatus(EntityStatus.Disabled.name());
        } else if (EntityStatus.Disabled.name().equalsIgnoreCase(existing.getStatus())) {
            existing.setStatus(EntityStatus.Enabled.name());
            existing.setLoginAttemptCount(0);
        }

        existing.setUpdatedAt(new Date());
        existing.setSkipAudit(true);
        existing.setSkipAuthorization(true);
        existing.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
        userRepository.save(existing);
        UpdateUserResponse response = new UpdateUserResponse();
        response.setData(modelMapper.map(existing, UpdateUserResponse.Data.class));
        return response;
    }

    public GetAllStaffResponse getAllStaff() {
        GetAllStaffResponse response = new GetAllStaffResponse();
        response.setData(new ArrayList<>());
        List<User> users = userRepository.findByCompanyCodeAndUserType(RequestUtil.getAuthToken().getCompanyCode(), GroupType.Customer.name());
        for (User user : users) {
            response.getData().add(modelMapper.map(user, GetAllStaffResponse.Data.class));
        }
        return response;
    }

    public GetAllStaffResponse getAllEnabledStaff() {
        GetAllStaffResponse response = new GetAllStaffResponse();
        response.setData(new ArrayList<>());
        List<User> users = userRepository.findEnabledUser(RequestUtil.getAuthToken().getCompanyCode(), GroupType.Customer.name(), EntityStatus.Enabled.name());
        for (User user : users) {
            response.getData().add(modelMapper.map(user, GetAllStaffResponse.Data.class));
        }
        return response;
    }

    public GetSupportedLanguageResponse getSupportedLanguages() {
        ArrayList<GetSupportedLanguageResponse.Data> data = new ArrayList<>();
        GetSupportedLanguageResponse response = new GetSupportedLanguageResponse();
        response.setData(data);
        for (Language language : Language.values()) {
            response.getData().add(GetSupportedLanguageResponse.Data.builder().lang(language.name()).key(language.getValue()).build());
        }
        return response;
    }

    public GetUserResponse getGetStaff(Long id) {
        GetUserResponse response = new GetUserResponse();
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new AppException(messageService.getMessage("User.Id.NotFound").replace("{0}", String.valueOf(id)));
        }
        response.setData(modelMapper.map(user, GetUserResponse.Data.class));
        return response;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDefaultSystemAdmin() {

        User user = userRepository.findByUsername(defaultAdminEmail);

        if (user == null) {
            log.info("About to create a default admin ({})", defaultAdminEmail);
            User newEntry = new User();
            newEntry.setCreatedBy("System");
            newEntry.setCreatedAt(new Date());
            newEntry.setEmail(defaultAdminEmail);
            newEntry.setPhone(defaultAdminPhone);
            newEntry.setPhoneVerified(true);
            newEntry.setEmailVerified(true);
            newEntry.setStaffName("Default Admin");
            newEntry.setLang("en");
            newEntry.setUserType(GroupType.SuperAdmin.name());
            newEntry.setCompanyCode(defaultCompanyCode);
            newEntry.setStatus(EntityStatus.Enabled.name());
            newEntry.setHasChangedPassword(true);
            newEntry.setSkipAudit(true);
            newEntry.setSkipAuthorization(true);
            newEntry = userRepository.save(newEntry);
            userLoginRepository.save(UserLogin.builder()
                    .password(passwordEncoder.encode("Password@123"))
                    .status(EntityStatus.Enabled.name())
                    .createdAt(new Date())
                    .userId(newEntry.getId())
                    .companyCode(newEntry.getCompanyCode())
                    .uuid(UUID.randomUUID().toString())
                    .build());
            log.info("Default admin ({}) created successfully", defaultAdminEmail);
        }
    }

    public SearchStaffResponse searchStaff(SearchStaffRequest dto) {
            dto.sanitize();
            String companyCode = RequestUtil.getAuthToken().getCompanyCode();
            Pageable pageable = PaginationUtil.getPageRequest(dto.getPageIndex(), dto.getPageSize());
            Page<User> filtered = userRepository.searchUser(
                    companyCode,
                    dto.getEmail(),
                    dto.getPhone(),
                    dto.getStaffName(),
                    dto.getSearchTerm(),
                    GroupType.Customer.name(),
                    dto.getStatus(),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    pageable
            );

            SearchStaffResponse response = new SearchStaffResponse();
            response.setData(new ArrayList<>());
            for (User user : filtered) {
                response.getData().add(modelMapper.map(user, SearchStaffResponse.Data.class));
            }
            response.setPageIndex(filtered.getNumber());
            response.setPageSize(filtered.getSize());
            response.setTotalContent(filtered.getTotalElements());
            response.setHasNextPage(filtered.hasNext());
            response.setHasPreviousPage(filtered.hasPrevious());
            response.setTotalPages(filtered.getTotalPages());
            return response;
        }

    public ViewUserTypesResponse getUserTypes() {

        ViewUserTypesResponse response = ViewUserTypesResponse.builder()
                .data(new ArrayList<>())
                .build();

        for (GroupType userType : GroupType.values()) {
            if (userType != GroupType.Customer) {
                response.getData().add(userType);
            }
        }

        return response;
    }
    public BlockedUsers getBlockedUsers() {
        String companyCode = RequestUtil.getAuthToken().getCompanyCode();
        List<User> blockedUsers = userRepository.findByCompanyCodeAndStatus(RequestUtil.getAuthToken().getCompanyCode(), EntityStatus.Disabled.name(), GroupType.Customer.name());
        BlockedUsers response = new BlockedUsers();
        response.setBlockedUsers(blockedUsers);
        return response;
    }

    public GetActiveStaffResponse getStaffByStatus(String status) {
        List<User> activeUsers = userRepository.findByStaffCount(RequestUtil.getAuthToken().getCompanyCode(), status, GroupType.Customer.name());
        return GetActiveStaffResponse.builder().data(activeUsers.size()).build();
    }

}
