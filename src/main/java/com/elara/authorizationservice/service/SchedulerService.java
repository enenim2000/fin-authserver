package com.elara.authorizationservice.service;

import com.elara.authorizationservice.domain.Company;
import com.elara.authorizationservice.domain.SystemSetting;
import com.elara.authorizationservice.dto.request.NotificationRequest;
import com.elara.authorizationservice.dto.response.SmsCreditBalanceResponse;
import com.elara.authorizationservice.enums.EntityStatus;
import com.elara.authorizationservice.enums.SystemSettingKeys;
import com.elara.authorizationservice.repository.CompanyRepository;
import com.elara.authorizationservice.repository.SystemSettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SchedulerService {

    @Value("${cron.cache.expiry.enabled}")
    boolean isEnabledDeleteCache;

    @Value("${cron.sms.balance.enabled}")
    boolean isEnabledSmsBalanceAlert;

    @Value("${cron.approval.notification.enabled}")
    boolean cronApprovalEnabled;

    @Value("${app.mail.sender}")
    String senderMail;

    private final NotificationCacheService cacheService;
    private final NotificationService notificationService;
    private final SmsService smsService;
    private final ApprovalService approvalService;
    private final MessageService messageService;
    private final CompanyRepository companyRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final MonitoringService monitoringService;

    public SchedulerService(NotificationCacheService cacheService,
                            NotificationService notificationService,
                            SmsService smsService,
                            ApprovalService approvalService,
                            MessageService messageService,
                            CompanyRepository companyRepository,
                            SystemSettingRepository systemSettingRepository,
                            MonitoringService monitoringService) {
        this.cacheService = cacheService;
        this.notificationService = notificationService;
        this.smsService = smsService;
        this.approvalService = approvalService;
        this.messageService = messageService;
        this.companyRepository = companyRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.monitoringService = monitoringService;
    }

    @Scheduled(cron = "${cron.cache.expiry.time}")
    public void deleteChatMessagesAutomatically() {
        if (isEnabledDeleteCache) {
            cacheService.deleteExpiredOtp();
        } else {
            log.info("CRON to delete expired otp cache is disabled");
        }
    }

    @Scheduled(cron = "${cron.sms.balance.time}")
    public void sendSmsAlertOnLowBalance() {
        if (isEnabledSmsBalanceAlert) {

            String name = SystemSettingKeys.SMS_MINIMUM_BALANCE.getValue();
            String platformSupport = SystemSettingKeys.PLATFORM_ADMIN_EMAIL.getValue();
            List<Company> companies = companyRepository.findAll();

            for (Company company : companies) {
                if (EntityStatus.Enabled.name().equals(company.getStatus())) {
                    SystemSetting setting = systemSettingRepository.findByCompanyCodeAndName(company.getCompanyCode(), name);
                    SystemSetting platformSupportSetting = systemSettingRepository.findByCompanyCodeAndName(company.getCompanyCode(), platformSupport);
                    if (setting != null && platformSupportSetting != null) {
                        double minimumCredit = Double.parseDouble(setting.getValue());
                        SmsCreditBalanceResponse response = smsService.getSmsCreditBalance();
                        if (response.getBalance() <= minimumCredit) {
                            String message = messageService.getMessage("message.email.sms-credit-balance");
                            message = message.replace("{0}", String.valueOf(response.getBalance()));
                            notificationService.sendEmail(NotificationRequest.builder()
                                            .message(message)
                                            .html(message)
                                            .companyCode(company.getCompanyCode())
                                            .recipientEmail(platformSupportSetting.getValue())
                                            .senderEmail(senderMail)
                                            .requiredValidation(false)
                                            .subject(messageService.getMessage("message.email.sms-credit-subject"))
                                    .build());
                        }
                    }
                }
            }

        } else {
            log.info("CRON to send notification alert on low sms credit balance disabled");
        }
    }

    @Scheduled(cron = "${cron.approval.notification.time}")
    public void sendApprovalStatusNotificationToRecipient() {
        if (cronApprovalEnabled) {
            log.info("CRON to send approval status notification to recipient started");
            approvalService.sendApprovalNotification();
        } else {
            log.info("CRON to send approval status notification to recipient is disabled");
        }
    }

}
