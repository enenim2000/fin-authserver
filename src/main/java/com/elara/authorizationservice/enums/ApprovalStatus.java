package com.elara.authorizationservice.enums;

public enum ApprovalStatus {

    Pending,
    InProgress,
    Approved,
    Rejected,
    Completed,
    Rework;

    public static boolean isValid(String value) {
        for (ApprovalStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
