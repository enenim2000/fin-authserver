package com.elara.authorizationservice.controller;

import com.elara.authorizationservice.auth.Permission;
import com.elara.authorizationservice.dto.request.SaveAllSystemSettingRequest;
import com.elara.authorizationservice.dto.request.SaveSystemSettingRequest;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/settings")
@RestController
@Tag(name = "Management System Settings", description = "Management System Settings")
public class SystemSettingController {

  final SystemSettingService systemSettingService;

  public SystemSettingController(SystemSettingService systemSettingService) {
    this.systemSettingService = systemSettingService;
  }

  @Operation(summary = "Update System Setting")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Update System Setting",
          content = {@Content(mediaType = "application/json",
              schema = @Schema(implementation = SaveSystemSettingResponse.class))})})
  @Permission("UPDATE_SYSTEM_SETTING")
  @PostMapping("/save")
  public ResponseEntity<SaveSystemSettingResponse> updateSetting(@Valid @RequestBody SaveSystemSettingRequest dto){
    return ResponseEntity.ok(systemSettingService.updateSetting(dto));
  }

  @Operation(summary = "Reset All System Settings")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Reset All System Settings",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ResetAllSystemSettingResponse.class))})})
  @Permission("RESET_ALL_SETTINGS")
  @PostMapping("/reset")
  public ResponseEntity<ResetAllSystemSettingResponse> resetAllSettings(){
    return ResponseEntity.ok(systemSettingService.resetAllSettings());
  }

  @Operation(summary = "Save All System Settings")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Save All System Settings",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = SaveAllSystemSettingResponse.class))})})
  @Permission("SAVE_ALL_SETTINGS")
  @PostMapping("/all/save")
  public ResponseEntity<SaveAllSystemSettingResponse> saveAllSettings(@Valid @RequestBody SaveAllSystemSettingRequest dto){
    return ResponseEntity.ok(systemSettingService.updateAllSettings(dto));
  }

  @Operation(summary = "View Settings Keys and Values")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Create or Update System Setting",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ViewSettingsKeyValuesResponse.class))})})
  //@Permission("VIEW_SETTINGS_KEY_VALUES")
  @GetMapping("/key-values")
  public ResponseEntity<ViewSettingsKeyValuesResponse> viewSettingKeyValues() {
    return ResponseEntity.ok(systemSettingService.getKeyValues());
  }

  @Operation(summary = "View All System Settings")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View All System Settings",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = SystemSettingsResponse.class))})})
  //@Permission("VIEW_ALL_SETTINGS")
  @GetMapping
  public ResponseEntity<SystemSettingsResponse> viewAllSettings(){
    return ResponseEntity.ok(systemSettingService.getSettings());
  }
}
