package com.elara.authorizationservice.enums;

public enum GroupType {
    Customer,
    Staff,
    Admin,
    SuperAdmin;

    public static boolean isValid(String value) {
        for (GroupType userType : values()) {
            if (userType.name().equals(value) && !GroupType.Customer.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
