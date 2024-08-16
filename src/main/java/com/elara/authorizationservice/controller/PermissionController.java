package com.elara.authorizationservice.controller;

import com.elara.authorizationservice.auth.Permission;
import com.elara.authorizationservice.dto.request.*;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Permission Management", description = "Permission Management")
public class PermissionController {

    final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "Add Application Permission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Add Application Permission",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreatePermissionResponse.class))})})
    @PostMapping("/permission/add")
    public ResponseEntity<CreatePermissionResponse> createApplicationPermission(@Valid @RequestBody CreatePermissionRequest dto) {
        return ResponseEntity.ok(permissionService.createApplicationPermission(dto));
    }

    @Operation(summary = "Create Group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create Group",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateGroupResponse.class))})})
    @Permission("CREATE_GROUP")
    @PostMapping("/group/create")
    public ResponseEntity<CreateGroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest dto) {
        return ResponseEntity.ok(permissionService.createGroup(dto));
    }

    @Operation(summary = "Update Group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update Group",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateGroupResponse.class))})})
    @Permission("UPDATE_GROUP")
    @PutMapping("/group/update")
    public ResponseEntity<UpdateGroupResponse> updateGroup(@Valid @RequestBody UpdateGroupRequest dto) {
        return ResponseEntity.ok(permissionService.updateGroup(dto));
    }

    @Operation(summary = "Get Group By Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Group By Id",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetGroupResponse.class))})})
    @GetMapping("/group/{id}")
    public ResponseEntity<GetGroupResponse> getGroup(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getGroup(id));
    }

    @Permission("VIEW_USER_PERMISSIONS_AND_GROUP")
    @Operation(summary = "Get User's Permission & Group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get User's Permission & Group",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserPermissionAndGroupResponse.class))})})
    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<UserPermissionAndGroupResponse> getUserPermissionAndGroup(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionService.getUserPermissionAndGroup(userId));
    }

    @Permission("VIEW_GROUP_PERMISSION")
    @Operation(summary = "View Group's Permission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "View Group's Permission",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetGroupPermissionsResponse.class))})})
    @GetMapping("/groups/{id}/permissions")
    public ResponseEntity<GetGroupPermissionsResponse> getGroupPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getGroupPermissions(id));
    }

    @Operation(summary = "Get All Groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Groups",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetAllGroupResponse.class))})})
    @GetMapping("/groups")
    public ResponseEntity<GetAllGroupResponse> getAllGroups() {
        return ResponseEntity.ok(permissionService.getGroups());
    }

    @Operation(summary = "Assign Group(s) to User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assign Group(s) to User",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AssignUserGroupResponse.class))})})
    @Permission("ASSIGN_USER_GROUP")
    @PostMapping("/user/group/assign")
    public ResponseEntity<AssignUserGroupResponse> assignUserGroup(@Valid @RequestBody AssignUserGroupRequest dto) {
        return ResponseEntity.ok(permissionService.assignGroupToUserMakerChecker(dto, false));
    }

    @Operation(summary = "Assign Permission(s) to User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assign Permission(s) to User",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AssignUserPermissionResponse.class))})})
    @Permission("ASSIGN_USER_PERMISSION")
    @PostMapping("/user/permission/assign")
    public ResponseEntity<AssignUserPermissionResponse> assignUserPermission(@Valid @RequestBody AssignUserPermissionRequest dto) {
        return ResponseEntity.ok(permissionService.assignPermissionToUserMakerChecker(dto, false));
    }

    @Operation(summary = "Assign Permission(s) to Group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assign Permission(s) to Group",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AssignGroupPermissionResponse.class))})})
    @Permission("ASSIGN_GROUP_PERMISSION")
    @PostMapping("/group/permission/assign")
    public ResponseEntity<AssignGroupPermissionResponse> assignGroupPermission(@Valid @RequestBody AssignGroupPermissionRequest dto) {
        return ResponseEntity.ok(permissionService.assignPermissionToGroupMakerChecker(dto, false));
    }

    @Operation(summary = "Sync Application Permission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sync Application Permission",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SyncPermissionResponse.class))})})
    @PostMapping("/permission/sync")
    public ResponseEntity<SyncPermissionResponse> syncApplicationPermission(@Valid @RequestBody SyncPermissionRequest dto) {
        return ResponseEntity.ok(permissionService.syncApplicationPermission(dto));
    }

    @Operation(summary = "Get Permission By Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Permission By Id",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetPermissionByIdResponse.class))})})
    @GetMapping("/permission/{id}")
    public ResponseEntity<GetPermissionByIdResponse> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @Operation(summary = "Get All Permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get All Permissions",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetAllPermissionsResponse.class))})})
    @GetMapping("/permissions")
    public ResponseEntity<GetAllPermissionsResponse> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }
}
