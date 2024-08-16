package com.elara.authorizationservice.enums;

public enum NotificationType  implements PersistableEnum<String> {
    EmailVerify,
    PhoneVerify,
    EmailResendVerify,
    PhoneResendVerify,
    TransactionVerify,
    SetPinVerify,
    ResetPinVerify,
    ResetPasswordVerify,

    DeleteProfileVerify;

    @Override
    public String getValue() {
        return null;
    }

    public static class Converter extends EnumValueTypeConverter<NotificationType, String> {
        public Converter() {
            super(NotificationType.class);
        }
    }
}
