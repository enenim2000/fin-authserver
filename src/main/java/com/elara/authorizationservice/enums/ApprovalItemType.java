package com.elara.authorizationservice.enums;

public enum ApprovalItemType {
    Loan,
    User,
    Group,
    UserGroupPermission,
    GroupPermission,
    UserPermission;

    public static boolean isValid(String value) {
        for (ApprovalItemType status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
