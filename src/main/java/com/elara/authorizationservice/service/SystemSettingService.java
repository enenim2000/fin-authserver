package com.elara.authorizationservice.service;

import com.elara.authorizationservice.auth.RequestUtil;
import com.elara.authorizationservice.domain.SystemSetting;
import com.elara.authorizationservice.dto.request.SaveAllSystemSettingRequest;
import com.elara.authorizationservice.dto.request.SaveSystemSettingRequest;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.enums.EntityStatus;
import com.elara.authorizationservice.enums.PlatformState;
import com.elara.authorizationservice.enums.SystemSettingKeys;
import com.elara.authorizationservice.exception.AppException;
import com.elara.authorizationservice.repository.SystemSettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SystemSettingService {

    @Value("${default.admin.email}")
    private String defaultPlatformAdmin;

    private final MessageService messageService;
    private final SystemSettingRepository systemSettingRepository;

    public SystemSettingService(MessageService messageService,
                                SystemSettingRepository systemSettingRepository) {
        this.messageService = messageService;
        this.systemSettingRepository = systemSettingRepository;
    }

    public SystemSetting findBySetting(String companyCode, String settingKey) {
        SystemSetting systemSetting = systemSettingRepository.findByCompanyCodeAndName(companyCode, settingKey);
        if (systemSetting == null) {
            String message = messageService.getMessage("system.settings.notFound");
            message = message.replace("{0}", settingKey);
            throw new AppException(message);
        }
        return systemSetting;
    }

    public ViewSettingsKeyValuesResponse getKeyValues() {

        ViewSettingsKeyValuesResponse response = new ViewSettingsKeyValuesResponse();
        response.setData(new HashMap<>());
        response.getData().put(SystemSettingKeys.PLATFORM_STATE.getValue(), Arrays.asList(PlatformState.values()));
        response.getData().put(SystemSettingKeys.CUSTOMER_SUPPORT_EMAIL.getValue(), "");
        response.getData().put(SystemSettingKeys.PLATFORM_ADMIN_EMAIL.getValue(), "");
        response.getData().put(SystemSettingKeys.CUSTOMER_SUPPORT_PHONE.getValue(), "");
        response.getData().put(SystemSettingKeys.SMS_MINIMUM_BALANCE.getValue(), "");

        response.getData().put(SystemSettingKeys.TRANSFER_SERVICE_STATE.getValue(), Arrays.asList(PlatformState.values()));
        response.getData().put(SystemSettingKeys.LOAN_SERVICE_STATE.getValue(), Arrays.asList(PlatformState.values()));
        response.getData().put(SystemSettingKeys.BILLS_PAYMENT_SERVICE_STATE.getValue(), Arrays.asList(PlatformState.values()));
        response.getData().put(SystemSettingKeys.PLATFORM_BACKEND_CHANNEL_STATE.getValue(), Arrays.asList(PlatformState.values()));
        response.getData().put(SystemSettingKeys.MOBILE_CHANNEL_STATE.getValue(), Arrays.asList(PlatformState.values()));

        return response;
    }

    public SystemSettingsResponse getSettings() {
        List<SystemSetting> systemSettings = systemSettingRepository.findByCompanyCode(RequestUtil.getAuthToken().getCompanyCode());
        SystemSettingsResponse response = SystemSettingsResponse.builder().build();
        response.setData(new ArrayList<>());
        for (SystemSetting systemSetting : systemSettings) {
            response.getData().add(SystemSettingsResponse.Data.builder()
                    .key(systemSetting.getName())
                    .value(systemSetting.getValue()).build());
        }
        return response;
    }

    public SaveSystemSettingResponse updateSetting(SaveSystemSettingRequest dto) {
        SystemSetting systemSetting = systemSettingRepository.findByCompanyCodeAndName(RequestUtil.getAuthToken().getCompanyCode(), dto.getKey());
        if (systemSetting == null) {
            throw new AppException(messageService.getMessage("system.settings.notFound").replace("{0}", dto.getKey()));
        }
        validateSettingValue(dto.getKey(), dto.getValue());
        systemSetting.setValue(dto.getValue());
        systemSetting.setUpdatedAt(new Date());
        systemSetting.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
        systemSettingRepository.save(systemSetting);
        return SaveSystemSettingResponse.builder().build();
    }


    public SaveAllSystemSettingResponse updateAllSettings(SaveAllSystemSettingRequest dto) {
        SaveAllSystemSettingResponse response = SaveAllSystemSettingResponse.builder().build();
        response.setData(new ArrayList<>());
        for (SaveAllSystemSettingRequest.Data data : dto.getData()) {
            SystemSetting systemSetting = systemSettingRepository.findByCompanyCodeAndName(RequestUtil.getAuthToken().getCompanyCode(), data.getKey());
            if (systemSetting == null) {
                log.info("SystemSetting with key {} not found during update and was skipped", data.getKey());
            } else {
                validateSettingValue(data.getKey(), data.getValue());
                systemSetting.setValue(data.getValue());
                systemSetting.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
                systemSetting.setUpdatedAt(new Date());
                systemSetting = systemSettingRepository.save(systemSetting);
                response.getData().add(SaveAllSystemSettingResponse.Data.builder()
                        .key(systemSetting.getName())
                        .value(systemSetting.getValue())
                        .build());
            }
        }
        return response;
    }

    private void validateSettingValue(String key, String value) {

    }

    public ResetAllSystemSettingResponse resetAllSettings() {

        ArrayList<SystemSetting> systemSettings = new ArrayList<>();
        List<SystemSetting> values = systemSettingRepository.findAll();
        Map<String, SystemSetting> keyValues = new HashMap<>();
        for (SystemSetting value : values) {
            if (!keyValues.containsKey(value.getName())) {
                keyValues.put(value.getName(), value);
            }
        }

        if (!keyValues.containsKey(SystemSettingKeys.PLATFORM_STATE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.PLATFORM_STATE.getValue())
                    .value(PlatformState.Active.name())
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.SMS_MINIMUM_BALANCE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.SMS_MINIMUM_BALANCE.getValue())
                    .value("1000")
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.CUSTOMER_SUPPORT_PHONE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.CUSTOMER_SUPPORT_PHONE.getValue())
                    .value("")
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.PLATFORM_ADMIN_EMAIL.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.PLATFORM_ADMIN_EMAIL.getValue())
                    .value("")
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.CUSTOMER_SUPPORT_EMAIL.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.CUSTOMER_SUPPORT_EMAIL.getValue())
                    .value("")
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.BILLS_PAYMENT_SERVICE_STATE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.BILLS_PAYMENT_SERVICE_STATE.getValue())
                    .value(PlatformState.Active.name())
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.TRANSFER_SERVICE_STATE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.TRANSFER_SERVICE_STATE.getValue())
                    .value(PlatformState.Active.name())
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.LOAN_SERVICE_STATE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.LOAN_SERVICE_STATE.getValue())
                    .value(PlatformState.Active.name())
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.MOBILE_CHANNEL_STATE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.MOBILE_CHANNEL_STATE.getValue())
                    .value(PlatformState.Active.name())
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        if (!keyValues.containsKey(SystemSettingKeys.PLATFORM_BACKEND_CHANNEL_STATE.getValue())) {
            systemSettings.add(SystemSetting.builder()
                    .companyCode(RequestUtil.getAuthToken().getCompanyCode())
                    .createdAt(new Date())
                    .createdBy(RequestUtil.getAuthToken().getUsername())
                    .name(SystemSettingKeys.PLATFORM_BACKEND_CHANNEL_STATE.getValue())
                    .value(PlatformState.Active.name())
                    .status(EntityStatus.Enabled.name())
                    .build());
        }

        systemSettingRepository.saveAll(systemSettings);

        return new ResetAllSystemSettingResponse();
    }

    public Map<String, SystemSetting> getSettingMap() {
        Map<String, SystemSetting> settingMap = new HashMap<>();
        List<SystemSetting> settings = systemSettingRepository.findByCompanyCode(RequestUtil.getAuthToken().getCompanyCode());;
        for (SystemSetting systemSetting : settings) {
            if (!settingMap.containsKey(systemSetting.getName())) {
                settingMap.put(systemSetting.getName(), systemSetting);
            }
        }
        return settingMap;
    }
}
