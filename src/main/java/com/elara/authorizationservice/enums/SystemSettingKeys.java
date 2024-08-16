package com.elara.authorizationservice.enums;

public enum SystemSettingKeys {

    PLATFORM_STATE("platform_state"),
    PLATFORM_ADMIN_EMAIL("platform_admin_email"), //For platform admins required notification e.g low minimum balance
    //SUPER_ADMIN_EMAILS("super_admin_emails"), //comma separated emails of all platform super admin
    CUSTOMER_SUPPORT_EMAIL("customer_support_email"),
    CUSTOMER_SUPPORT_PHONE("contact_support_phone"),
    SMS_MINIMUM_BALANCE("sms_minimum_balance"),
    TRANSFER_SERVICE_STATE("transfer_service_state"),
    LOAN_SERVICE_STATE("loan_service_state"),
    BILLS_PAYMENT_SERVICE_STATE("bills_payment_service_state"),
    MOBILE_CHANNEL_STATE("mobile_channel_state"),
    PLATFORM_BACKEND_CHANNEL_STATE("platform_backend_channel_state");

    final String value;

    public String getValue(){
        return value;
    }

    SystemSettingKeys(String value) {
        this.value = value;
    }

}
