package com.elara.authorizationservice.service;

import com.elara.authorizationservice.auth.Permission;
import com.elara.authorizationservice.auth.RequestUtil;
import com.elara.authorizationservice.domain.*;
import com.elara.authorizationservice.dto.request.*;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.enums.ApprovalItemType;
import com.elara.authorizationservice.enums.CrudOperation;
import com.elara.authorizationservice.enums.EntityStatus;
import com.elara.authorizationservice.enums.GroupType;
import com.elara.authorizationservice.exception.AppException;
import com.elara.authorizationservice.repository.*;
import com.elara.authorizationservice.util.HashUtil;
import com.elara.authorizationservice.util.JsonConverter;
import com.elara.authorizationservice.util.PermissionUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Slf4j
@Service
public class PermissionService {

    final ModelMapper modelMapper;
    final MessageService messageService;
    private final ApplicationRepository applicationRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserGroupPermissionRepository userGroupPermissionRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ApplicationAccountRepository applicationAccountRepository;
    private final ApplicationPermissionRepository applicationPermissionRepository;
    private final AuditRepository auditRepository;

    @Value("${spring.application.name}")
    private String appName;


    @Value("${default.companyCode}")
    private String defaultCompanyCode;

    public PermissionService(
            ApplicationRepository applicationRepository,
            ModelMapper modelMapper,
            MessageService messageService,
            GroupRepository groupRepository,
            UserGroupRepository userGroupRepository,
            UserGroupPermissionRepository userGroupPermissionRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            ApplicationAccountRepository applicationAccountRepository,
            ApplicationPermissionRepository applicationPermissionRepository,
            AuditRepository auditRepository) {
        this.applicationRepository = applicationRepository;
        this.modelMapper = modelMapper;
        this.messageService = messageService;
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
        this.userGroupPermissionRepository = userGroupPermissionRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.applicationAccountRepository = applicationAccountRepository;
        this.applicationPermissionRepository = applicationPermissionRepository;
        this.auditRepository = auditRepository;
    }

    public CreateGroupResponse createGroup(CreateGroupRequest dto) {
        Company company = companyRepository.findByCompanyCode(dto.getCompanyCode());
        if (company == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        Group existing = groupRepository.findByGroupNameAndCompanyCode(dto.getGroupName(), company.getCompanyCode());
        if (existing != null) {
            throw new AppException(messageService.getMessage("Group.Exist"));
        }

        Group newEntry = modelMapper.map(dto, Group.class);
        newEntry.setCreatedBy(RequestUtil.getAuthToken().getUsername());
        newEntry.setCreatedAt(new Date());
        newEntry.setStatus(EntityStatus.Enabled.name());
        newEntry.setCompanyCode(company.getCompanyCode());

        if (GroupType.SuperAdmin.name().equals(RequestUtil.getUserType())) {
            newEntry.setSkipAudit(true);
            newEntry.setSkipAuthorization(true);
        } else {
            newEntry.setSkipAudit(false);
            newEntry.setSkipAuthorization(false);
            newEntry.setResponseType(AuthorizationGroupResponse.class.getName());
        }

        newEntry = groupRepository.save(newEntry);
        CreateGroupResponse response = new CreateGroupResponse();
        response.setData(modelMapper.map(newEntry, CreateGroupResponse.Data.class));
        return response;
    }

    public UpdateGroupResponse updateGroup(UpdateGroupRequest dto) {
        Company company = companyRepository.findByCompanyCode(dto.getCompanyCode());
        if (company == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        Group existing = groupRepository.findByGroupNameAndCompanyCode(dto.getGroupName(), company.getCompanyCode());
        if (existing == null) {
            throw new AppException(messageService.getMessage("Group.NotFound"));
        }

        modelMapper.map(dto, existing);
        existing.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
        existing.setUpdatedAt(new Date());
        existing.setCompanyCode(company.getCompanyCode());
        if (GroupType.SuperAdmin.name().equals(RequestUtil.getUserType())) {
            existing.setSkipAudit(true);
            existing.setSkipAuthorization(true);
        } else {
            existing.setSkipAudit(false);
            existing.setSkipAuthorization(false);
            existing.setResponseType(AuthorizationGroupResponse.class.getName());
        }
        existing = groupRepository.save(existing);
        UpdateGroupResponse response = new UpdateGroupResponse();
        response.setData(modelMapper.map(existing, UpdateGroupResponse.Data.class));
        return response;
    }

    public AssignUserGroupResponse assignGroupToUserMakerChecker(AssignUserGroupRequest dto, boolean skipAuthorization) {
        if (skipAuthorization || GroupType.SuperAdmin.name().equalsIgnoreCase(RequestUtil.getUserType())) {
            return assignGroupToUser(dto);
        }
        Optional<User> optionalUser = userRepository.findById(dto.getUserId());
        if (optionalUser.isEmpty()) {
            throw new AppException(messageService.getMessage("User.NotFound"));
        }

        User user = optionalUser.get();
        List<UserGroup> userGroups = userGroupRepository.findByUserIdAndCompanyCode(user.getId(), dto.getCompanyCode());
        List<Long> existingGroupIds = new ArrayList<>();
        for (UserGroup userGroup : userGroups) {
            existingGroupIds.add(userGroup.getGroupId());
        }

        Audit audit = new Audit();
        audit.setResponseType(HashMap.class.getName());
        audit.setMaker(RequestUtil.getAuthToken().getEmail());
        audit.setChecker(null);
        audit.setStatus(EntityStatus.Pending.name());
        audit.setUserType(RequestUtil.getUserType());
        audit.setCreatedBy(RequestUtil.getAuthToken().getEmail());
        audit.setCreatedAt(new Date());
        audit.setBefore(null);
        audit.setAfter(null);
        audit.setApprovalRequired(true);
        audit.setApprovalEmail("[]");
        audit.setApprovalDependency(JsonConverter.getJsonRecursive(dto));
        audit.setApprovalSms("[]");
        audit.setEntityId(0L);
        audit.setEntity(null);
        audit.setApprovalItemType(ApprovalItemType.UserGroupPermission.name());
        audit.setDescription("Assign group(s) to user (" + user.getStaffName()  + ")");
        if (dto.getGroupIds() != null &&  !dto.getGroupIds().isEmpty()) {
            audit.setAction(CrudOperation.Create.name());
            audit.setAfter(JsonConverter.getJsonRecursive(getGroupName(dto.getGroupIds())));
        }
        if (!existingGroupIds.isEmpty()) {
            audit.setAction(CrudOperation.Update.name());
            audit.setBefore(JsonConverter.getJsonRecursive(getGroupName(existingGroupIds)));
        }
        AssignUserGroupResponse response = new AssignUserGroupResponse();
        response.setResponseMessage("The group permission assignment submitted for approval");
        auditRepository.save(audit);
        return response;
    }

    public AssignGroupPermissionResponse assignPermissionToGroupMakerChecker(AssignGroupPermissionRequest dto, boolean skipAuthorization) {
        if (skipAuthorization || GroupType.SuperAdmin.name().equalsIgnoreCase(RequestUtil.getUserType())) {
            return assignPermissionToGroup(dto);
        }
        Optional<Group> optionalGroup = groupRepository.findById(dto.getGroupId());
        if (optionalGroup.isEmpty()) {
            throw new AppException(messageService.getMessage("Group.NotFound"));
        }

        Group group = optionalGroup.get();
        List<Long> existingPermissionIds = new ArrayList<>();

        List<UserGroupPermission> existingPermissions = userGroupPermissionRepository.findByGroupIdAndCompanyCode(group.getId(), dto.getCompanyCode());

        for (UserGroupPermission permission : existingPermissions) {
            existingPermissionIds.add(permission.getApplicationPermissionId());
        }

        Audit audit = new Audit();
        audit.setResponseType(HashMap.class.getName());
        audit.setMaker(RequestUtil.getAuthToken().getEmail());
        audit.setChecker(null);
        audit.setStatus(EntityStatus.Pending.name());
        audit.setUserType(RequestUtil.getUserType());
        audit.setCreatedBy(RequestUtil.getAuthToken().getEmail());
        audit.setCreatedAt(new Date());
        audit.setBefore(null);
        audit.setAfter(null);
        audit.setApprovalRequired(true);
        audit.setApprovalEmail("[]");
        audit.setApprovalDependency(JsonConverter.getJsonRecursive(dto));
        audit.setApprovalSms("[]");
        audit.setEntityId(0L);
        audit.setEntity(null);
        audit.setApprovalItemType(ApprovalItemType.GroupPermission.name());
        audit.setDescription("Assign permission(s) to group (" + group.getGroupName()  + ")");
        if (dto.getPermissionIds() != null &&  !dto.getPermissionIds().isEmpty()) {
            audit.setAction(CrudOperation.Create.name());
            audit.setAfter(JsonConverter.getJsonRecursive(getGroupPermissionName(dto.getPermissionIds())));
        }
        if (!existingPermissions.isEmpty()) {
            audit.setAction(CrudOperation.Update.name());
            audit.setBefore(JsonConverter.getJsonRecursive(getGroupPermissionName(existingPermissionIds)));
        }
        AssignGroupPermissionResponse response = new AssignGroupPermissionResponse();
        response.setResponseMessage("The request to assign permission(s) to group has been submitted for approval");
        auditRepository.save(audit);
        return response;
    }

    public AssignUserPermissionResponse assignPermissionToUserMakerChecker(AssignUserPermissionRequest dto, boolean skipAuthorization) {
        if (skipAuthorization || GroupType.SuperAdmin.name().equalsIgnoreCase(RequestUtil.getUserType())) {
            return assignPermissionToUser(dto);
        }
        Optional<User> optionalUser = userRepository.findById(dto.getUserId());
        if (optionalUser.isEmpty()) {
            throw new AppException(messageService.getMessage("User.NotFound"));
        }

        User user = optionalUser.get();
        List<String> existingPermissionIds = new ArrayList<>();

        List<ApplicationAccount> existingPermissions = applicationAccountRepository.findByUserIdAndCompanyCode(user.getId(), dto.getCompanyCode());

        for (ApplicationAccount userPermission : existingPermissions) {
            existingPermissionIds.add(userPermission.getPermissionId());
        }

        Audit audit = new Audit();
        audit.setResponseType(HashMap.class.getName());
        audit.setMaker(RequestUtil.getAuthToken().getEmail());
        audit.setChecker(null);
        audit.setStatus(EntityStatus.Pending.name());
        audit.setUserType(RequestUtil.getUserType());
        audit.setCreatedBy(RequestUtil.getAuthToken().getEmail());
        audit.setCreatedAt(new Date());
        audit.setBefore(null);
        audit.setAfter(null);
        audit.setApprovalRequired(true);
        audit.setApprovalEmail("[]");
        audit.setApprovalDependency(JsonConverter.getJsonRecursive(dto));
        audit.setApprovalSms("[]");
        audit.setEntityId(0L);
        audit.setEntity(null);
        audit.setApprovalItemType(ApprovalItemType.UserPermission.name());
        audit.setDescription("Assign permission(s) to user (" + user.getStaffName()  + ")");
        if (dto.getPermissionIds() != null &&  !dto.getPermissionIds().isEmpty()) {
            audit.setAction(CrudOperation.Create.name());
            audit.setAfter(JsonConverter.getJsonRecursive(getPermissionName(dto.getPermissionIds())));
        }
        if (!existingPermissions.isEmpty()) {
            audit.setAction(CrudOperation.Update.name());
            audit.setBefore(JsonConverter.getJsonRecursive(getPermissionName(existingPermissionIds)));
        }
        AssignUserPermissionResponse response = new AssignUserPermissionResponse();
        response.setResponseMessage("The user permission assignment submitted for approval");
        auditRepository.save(audit);
        return response;
    }

    private Map<String, String> getGroupName(List<Long> groupIds) {
        Map<String, String> map = new HashMap<>();
        int count = 0;
        for (Long groupId : groupIds) {
            count = count + 1;
            Group group = groupRepository.findById(groupId).orElse(null);
            if (group !=  null) {
                map.put("Group " + count, group.getGroupName());
            }
        }
        return map;
    }

    private Map<String, String> getPermissionName(List<String> permissions) {
        Map<String, String> map = new HashMap<>();
        int count = 0;
        for (String permissionId : permissions) {
            count = count + 1;
            ApplicationPermission permission = applicationPermissionRepository.findByPermissionId(permissionId);
            if (permission !=  null) {
                map.put("Permission " + count, permission.getPermission());
            }
        }
        return map;
    }

    private Map<String, String> getGroupPermissionName(List<Long> permissions) {
        Map<String, String> map = new HashMap<>();
        int count = 0;
        for (Long permissionId : permissions) {
            count = count + 1;
            ApplicationPermission permission = applicationPermissionRepository.findById(permissionId).orElse(null);
            if (permission !=  null) {
                map.put("Permission " + count, permission.getPermission());
            }
        }
        return map;
    }

    public AssignUserGroupResponse assignGroupToUser(AssignUserGroupRequest dto) {
        Company existing = companyRepository.findByCompanyCode(dto.getCompanyCode());
        if (existing == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        Optional<User> optionalUser = userRepository.findById(dto.getUserId());
        if (optionalUser.isEmpty()) {
            throw new AppException(messageService.getMessage("User.NotFound"));
        }

        User user = optionalUser.get();

        List<Long> deleteGroupIds = new ArrayList<>();
        List<UserGroup> newUserGroups = new ArrayList<>();

        List<UserGroup> userGroups = userGroupRepository.findByUserIdAndCompanyCode(user.getId(), dto.getCompanyCode());
        for (UserGroup userGroup : userGroups) {
            if (!dto.getGroupIds().contains(userGroup.getGroupId())) {
                deleteGroupIds.add(userGroup.getGroupId());
            }
        }

        for (Long groupId : dto.getGroupIds()) {
            Optional<Group> optionalGroup = groupRepository.findById(groupId);
            if (optionalGroup.isPresent()) {
                UserGroup existingUserGroup = userGroupRepository.findByCompanyCodeAndUserIdAndGroupId(dto.getCompanyCode(), dto.getUserId(), groupId);
                if (existingUserGroup == null) {
                    newUserGroups.add(UserGroup.builder()
                            .groupId(groupId)
                            .companyCode(dto.getCompanyCode())
                            .userId(user.getId())
                            .createdAt(new Date())
                            .createdBy(RequestUtil.getAuthToken().getUsername())
                            .build());
                }
            }
        }

        userGroupRepository.saveAll(newUserGroups);
        userGroupRepository.deleteByCompanyCodeAndUserIdAndGroupIdIn(dto.getCompanyCode(), user.getId(), deleteGroupIds);

        return new AssignUserGroupResponse();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AssignUserPermissionResponse assignPermissionToUser(AssignUserPermissionRequest dto) {
        Company existing = companyRepository.findByCompanyCode(dto.getCompanyCode());
        if (existing == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        Optional<User> optionalUser = userRepository.findById(dto.getUserId());
        if (optionalUser.isEmpty()) {
            throw new AppException(messageService.getMessage("User.NotFound"));
        }

        User user = optionalUser.get();

        List<String> deletePermissionIds = new ArrayList<>();
        List<ApplicationAccount> newPermissionIds = new ArrayList<>();

        List<ApplicationAccount> userPermissions = applicationAccountRepository.findByUserIdAndCompanyCode(user.getId(), dto.getCompanyCode());
        for (ApplicationAccount userPermission : userPermissions) {
            if (!dto.getPermissionIds().contains(userPermission.getPermissionId())) {
                deletePermissionIds.add(userPermission.getPermissionId());
            }
        }

        for (String permissionId : dto.getPermissionIds()) {
            ApplicationPermission applicationPermission = applicationPermissionRepository.findByPermissionId(permissionId);
            if (applicationPermission != null) {
                ApplicationAccount applicationAccount = applicationAccountRepository.findByCompanyCodeAndUserIdAndPermissionId(dto.getCompanyCode(), dto.getUserId(), permissionId);
                if (applicationAccount == null) {
                    newPermissionIds.add(ApplicationAccount.builder()
                            .permissionId(permissionId)
                            .companyCode(dto.getCompanyCode())
                            .userId(user.getId())
                            .createdAt(new Date())
                            .createdBy(RequestUtil.getAuthToken().getUsername())
                            .status(EntityStatus.Enabled.name())
                            .build());
                }
            }
        }

        applicationAccountRepository.saveAll(newPermissionIds);
        applicationAccountRepository.deleteByCompanyCodeAndUserIdAndPermissionIdIn(dto.getCompanyCode(), user.getId(), deletePermissionIds);

        return new AssignUserPermissionResponse();
    }

    public SyncPermissionResponse syncApplicationPermission(SyncPermissionRequest dto) {
        Application application = applicationRepository.findByAppName(dto.getAppName());
        if (application == null) {
            throw new AppException(messageService.getMessage("App.NotFound"));
        }

        for (SyncPermissionRequest.Data data : dto.getPermissions()) {
            String permissionId = HashUtil.getHash(dto.getAppName() + data.getPermission());
            ApplicationPermission applicationPermission = applicationPermissionRepository.findByPermissionId(permissionId);
            if (applicationPermission == null) {
                applicationPermissionRepository.save(ApplicationPermission.builder()
                        .applicationId(application.getId())
                        .permissionId(permissionId)
                        .description(data.getDescription())
                        .isSecured(data.isSecured())
                        .httpMethod(data.getHttpMethod())
                        .permission(data.getPermission())
                        .uriPath(data.getUriPath())
                        .status(EntityStatus.Enabled.name())
                        .createdAt(new Date())
                        .createdBy(dto.getAppName())
                        .build());
            } else {
                applicationPermission.setPermission(data.getPermission());
                applicationPermission.setDescription(data.getDescription());
                applicationPermission.setSecured(data.isSecured());
                applicationPermission.setUpdatedAt(new Date());
                applicationPermission.setUpdatedBy(dto.getAppName());
                applicationPermissionRepository.save(applicationPermission);
            }
        }

        return new SyncPermissionResponse();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AssignGroupPermissionResponse assignPermissionToGroup(AssignGroupPermissionRequest dto) {
        Company existing = companyRepository.findByCompanyCode(dto.getCompanyCode());
        if (existing == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        Optional<Group> optionalGroup = groupRepository.findById(dto.getGroupId());
        if (optionalGroup.isEmpty()) {
            throw new AppException(messageService.getMessage("Group.NotFound"));
        }

        Group group = optionalGroup.get();

        List<Long> deletePermissionIds = new ArrayList<>();
        List<UserGroupPermission> newPermissions = new ArrayList<>();

        List<UserGroupPermission> groupPermissions = userGroupPermissionRepository.findByGroupIdAndCompanyCode(group.getId(), dto.getCompanyCode());
        for (UserGroupPermission groupPermission : groupPermissions) {
            if (!dto.getPermissionIds().contains(groupPermission.getApplicationPermissionId())) {
                deletePermissionIds.add(groupPermission.getApplicationPermissionId());
            }
        }

        for (Long applicationPermissionId : dto.getPermissionIds()) {
            Optional<ApplicationPermission> applicationPermission = applicationPermissionRepository.findById(applicationPermissionId);
            if (applicationPermission.isPresent()) {
                UserGroupPermission userGroupPermission = userGroupPermissionRepository.findByCompanyCodeAndGroupIdAndApplicationPermissionId(dto.getCompanyCode(), dto.getGroupId(), applicationPermissionId);
                if (userGroupPermission == null) {
                    newPermissions.add(UserGroupPermission.builder()
                            .companyCode(dto.getCompanyCode())
                            .groupId(group.getId())
                            .applicationPermissionId(applicationPermissionId)
                            .createdAt(new Date())
                            .createdBy(RequestUtil.getAuthToken().getUsername())
                            .status(EntityStatus.Enabled.name())
                            .build());
                }
            }
        }

        userGroupPermissionRepository.saveAll(newPermissions);
        userGroupPermissionRepository.deleteByCompanyCodeAndGroupIdAndApplicationPermissionIdIn(dto.getCompanyCode(), group.getId(), deletePermissionIds);

        return new AssignGroupPermissionResponse();
    }

    public CreatePermissionResponse createApplicationPermission(CreatePermissionRequest dto) {

        Application application = applicationRepository.findByAppName(dto.getAppName());
        if (application == null) {
            throw new AppException(messageService.getMessage("App.NotFound"));
        }

        for (CreatePermissionRequest.Data data : dto.getPermissions()) {
            String permissionId = HashUtil.getHash(application.getAppName() + data.getPermission());
            ApplicationPermission applicationPermission = applicationPermissionRepository.findByPermissionId(permissionId);
            if (applicationPermission == null) {
                applicationPermission = new ApplicationPermission();
                applicationPermission.setApplicationId(application.getId());
                applicationPermission.setPermissionId(permissionId);
                applicationPermission.setCreatedBy(RequestUtil.getAuthToken().getUsername());
                applicationPermission.setPermission(data.getPermission());
                applicationPermission.setSecured(true);
                applicationPermission.setCreatedAt(new Date());
                applicationPermission.setStatus(EntityStatus.Enabled.name());
                applicationPermission.setDescription(data.getDescription());
                applicationPermission.setHttpMethod(data.getHttpMethod());
                applicationPermission.setUriPath(data.getUriPath());
                applicationPermissionRepository.save(applicationPermission);
            }
        }

        return new CreatePermissionResponse();
    }

    public SyncPermissionRequest generatePermissionRequest(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        Map<RequestMappingInfo, HandlerMethod> endpoints = requestMappingHandlerMapping.getHandlerMethods();
        Iterator<Map.Entry<RequestMappingInfo, HandlerMethod>> it = endpoints.entrySet().iterator();
        HandlerMethod handlerMethod;
        RequestMappingInfo requestInfo;
        String permission;
        SyncPermissionRequest syncPermissionRequest = new SyncPermissionRequest();
        syncPermissionRequest.setAppName(appName);
        syncPermissionRequest.setPermissions(new ArrayList<>());
        while (it.hasNext()) {
            Map.Entry<RequestMappingInfo, HandlerMethod> pair = it.next();
            handlerMethod = pair.getValue();
            requestInfo = pair.getKey();
            if (handlerMethod.hasMethodAnnotation(Permission.class)) {
                permission = handlerMethod.getMethod().getDeclaredAnnotation(Permission.class).value();
                Optional<String> pathUri = requestInfo.getDirectPaths().stream().findFirst();

                String method = HttpMethod.POST.name();
                if (handlerMethod.getMethod().isAnnotationPresent(GetMapping.class)) {
                    method = HttpMethod.GET.name();
                } else if (handlerMethod.getMethod().isAnnotationPresent(PutMapping.class)) {
                    method = HttpMethod.PUT.name();
                } else if (handlerMethod.getMethod().isAnnotationPresent(PatchMapping.class)) {
                    method = HttpMethod.PATCH.name();
                } else if (handlerMethod.getMethod().isAnnotationPresent(DeleteMapping.class)) {
                    method = HttpMethod.DELETE.name();
                }

                String description = !handlerMethod.getMethod().isAnnotationPresent(Operation.class) ? "" : handlerMethod.getMethod().getDeclaredAnnotation(Operation.class).summary();
                syncPermissionRequest.getPermissions().add(
                        SyncPermissionRequest.Data.builder()
                                .permission(permission)
                                .description(description)
                                .httpMethod(method)
                                .isSecured(true)
                                .uriPath(pathUri.orElse(""))
                                .build());
            }
        }

        return syncPermissionRequest;
    }

    public GetGroupResponse getGroup(Long id) {
        Company company = companyRepository.findByCompanyCode(RequestUtil.getAuthToken().getCompanyCode());
        if (company == null) {
            throw new AppException(messageService.getMessage("Company.NotFound"));
        }

        Group existing = groupRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new AppException(messageService.getMessage("Group.NotFound"));
        }
        GetGroupResponse response = new GetGroupResponse();
        response.setData(modelMapper.map(existing, GetGroupResponse.Data.class));
        return response;
    }

    public GetAllGroupResponse getGroups() {
        GetAllGroupResponse response = new GetAllGroupResponse();
        response.setData(new ArrayList<>());
        List<Group> groups = groupRepository.findByCompanyCode(RequestUtil.getAuthToken().getCompanyCode(), EntityStatus.Enabled.name());
        for (Group group : groups) {
            response.getData().add(modelMapper.map(group, GetAllGroupResponse.Data.class));
        }
        return response;
    }

    public GetPermissionByIdResponse getPermissionById(Long id) {
        ApplicationPermission permission = applicationPermissionRepository.findById(id).orElse(null);
        if (permission == null) {
            throw new AppException(messageService.getMessage("Permission.NotFound"));
        }
        GetPermissionByIdResponse response = new GetPermissionByIdResponse();
        response.setData(modelMapper.map(permission, GetPermissionByIdResponse.Data.class));
        return response;
    }

    public GetAllPermissionsResponse getAllPermissions() {
        GetAllPermissionsResponse response = new GetAllPermissionsResponse();
        response.setData(new ArrayList<>());
        List<ApplicationPermission> permissions = applicationPermissionRepository.findAll();
        for (ApplicationPermission permission : permissions) {
            if (PermissionUtil.EXCLUDED_PERMISSIONS.contains(permission.getPermissionId())) {
                continue;
            }
            if (PermissionUtil.getCustomerPermissionMap().containsKey(permission.getPermissionId())) {
                continue;
            }
            response.getData().add(modelMapper.map(permission, GetAllPermissionsResponse.Data.class));
        }
        return response;
    }

    public UserPermissionAndGroupResponse getUserPermissionAndGroup(Long userId) {

        UserPermissionAndGroupResponse response = new UserPermissionAndGroupResponse();
        response.setData(UserPermissionAndGroupResponse.Data.builder().build());
        response.getData().setUserPermissions(new ArrayList<>());
        response.getData().setUserGroups(new ArrayList<>());

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new AppException(messageService.getMessage("User.NotFound"));
        }

        List<ApplicationAccount> userPermissions = applicationAccountRepository.findByUserIdAndCompanyCode(user.getId(), RequestUtil.getAuthToken().getCompanyCode());
        for (ApplicationAccount userPermission : userPermissions) {
            response.getData().getUserPermissions().add(modelMapper.map(userPermission, UserPermissionAndGroupResponse.UserPermission.class));
        }

        List<UserGroup> userGroups = userGroupRepository.findByUserIdAndCompanyCode(user.getId(), RequestUtil.getAuthToken().getCompanyCode());
        for (UserGroup userGroup : userGroups) {
            response.getData().getUserGroups().add(modelMapper.map(userGroup, UserPermissionAndGroupResponse.UserGroup.class));
        }

        return response;
    }

    public GetGroupPermissionsResponse getGroupPermissions(Long id) {

        GetGroupPermissionsResponse response = new GetGroupPermissionsResponse();
        response.setData(new ArrayList<>());

        Group group = groupRepository.findById(id).orElse(null);
        if (group == null) {
            throw new AppException(messageService.getMessage("Group.NotFound"));
        }

        List<UserGroupPermission> groupPermissions = userGroupPermissionRepository.findByGroupIdAndCompanyCode(id, RequestUtil.getAuthToken().getCompanyCode());
        for (UserGroupPermission groupPermission : groupPermissions) {
            response.getData().add(GetGroupPermissionsResponse.Data.builder()
                    .permissionId(groupPermission.getApplicationPermissionId()).build());
        }

        return response;
    }

    public void createDefaultGroup() {
        Group existing = groupRepository.findByGroupNameAndCompanyCode(GroupType.Customer.name(), defaultCompanyCode);
        if (existing == null) {
            Group newEntry = new Group();
            newEntry.setCompanyCode(defaultCompanyCode);
            newEntry.setDescription("Customer Group");
            newEntry.setGroupName(GroupType.Customer.name());
            newEntry.setSkipAuthorization(true);
            newEntry.setSkipAudit(true);
            newEntry.setCreatedBy("System");
            newEntry.setCreatedAt(new Date());
            newEntry.setStatus(EntityStatus.Enabled.name());
            groupRepository.save(newEntry);
        }
    }
}
