package com.elara.authorizationservice.controller;

import com.elara.authorizationservice.auth.Permission;
import com.elara.authorizationservice.dto.request.CreateUserRequest;
import com.elara.authorizationservice.dto.request.SearchStaffRequest;
import com.elara.authorizationservice.dto.request.UpdateUserRequest;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.enums.EntityStatus;
import com.elara.authorizationservice.service.UserService;
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
@Tag(name = "Staff Account Management", description = "Staff Account Management")
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "View Supported Languages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "View Supported Languages",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetSupportedLanguageResponse.class))})})
    @GetMapping("/user/languages")
    public ResponseEntity<GetSupportedLanguageResponse> getSupportedLanguages() {
        return ResponseEntity.ok(userService.getSupportedLanguages());
    }

    @Operation(summary = "Create New Staff Account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create New Staff Account",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateUserResponse.class))})})
    @Permission("CREATE_STAFF")
    @PostMapping("/user/create")
    public ResponseEntity<CreateUserResponse> createNewUser(@Valid @RequestBody CreateUserRequest dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @Operation(summary = "Update Staff Account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update Staff Account",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserResponse.class))})})
    @Permission("UPDATE_STAFF")
    @PutMapping("/user/update")
    public ResponseEntity<UpdateUserResponse> updateUser(@Valid @RequestBody UpdateUserRequest dto) {
        return ResponseEntity.ok(userService.updateUser(dto));
    }

    @Operation(summary = "Enabled or Disable Staff Account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enabled or Disable User Account",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserResponse.class))})})
    @Permission("ENABLE_DISABLE_USER")
    @PostMapping("/user/{id}/toggle")
    public ResponseEntity<UpdateUserResponse> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUser(id));
    }

    @Operation(summary = "View All Staff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "View All Staff",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetAllStaffResponse.class))})})
    @Permission("VIEW_ALL_STAFF")
    @PostMapping("/user")
    public ResponseEntity<GetAllStaffResponse> getAllStaff() {
        return ResponseEntity.ok(userService.getAllStaff());
    }

    @Operation(summary = "View Enabled Staff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "View Enabled Staff",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetAllStaffResponse.class))})})
    @Permission("VIEW_ENABLED_STAFF")
    @PostMapping("/enabled/users")
    public ResponseEntity<GetAllStaffResponse> getEnabledStaff() {
        return ResponseEntity.ok(userService.getAllEnabledStaff());
    }

    @Operation(summary = "Search Staff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search Staff",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SearchStaffResponse.class))})})
    @Permission("SEARCH_STAFF")
    @PostMapping("/user/search")
    public ResponseEntity<SearchStaffResponse> searchStaff(@RequestBody SearchStaffRequest dto) {
        return ResponseEntity.ok(userService.searchStaff(dto));
    }

  @Operation(summary = "Get Staff")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Get Staff",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = GetUserResponse.class))})})
  @GetMapping("/user/{id}")
  public ResponseEntity<GetUserResponse> getGetStaff(@PathVariable Long id){
    return ResponseEntity.ok(userService.getGetStaff(id));
  }

  @Operation(summary = "View User Types")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View User Types",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ViewUserTypesResponse.class))})})
  @GetMapping("/user/types")
  public ResponseEntity<ViewUserTypesResponse> getUserTypes(){
    return ResponseEntity.ok(userService.getUserTypes());
  }

    @Operation(summary = "Get Number of Active Staff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Number of Active Staff",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetActiveStaffResponse.class))})})
    @GetMapping("/user/active")
    public ResponseEntity<GetActiveStaffResponse> getActiveStaff(){
        return ResponseEntity.ok(userService.getStaffByStatus(EntityStatus.Enabled.name()));
    }

/*    @Operation(summary = "Get Staff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Staff",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetUserResponse.class))})})
    @GetMapping("/user/{id}")
    public ResponseEntity<GetUserResponse> getGetStaff(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getGetStaff(id));
    }*/

    @Operation(summary = "Get Blocked Users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Blocked Users",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = BlockedUsers.class))})})
    @GetMapping("/user/blocked")
    public BlockedUsers getBlockedUsers() {
        System.out.println("Get Blocked Users");
        return userService.getBlockedUsers();
    }
}
