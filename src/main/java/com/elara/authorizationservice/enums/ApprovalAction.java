package com.elara.authorizationservice.enums;

public enum ApprovalAction {

    Approve,
    Reject,
    Rework,

    Recall;

    public static boolean isValid(String value) {
        for (ApprovalAction status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
