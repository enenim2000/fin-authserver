package com.elara.authorizationservice.enums;

public enum CrudOperation {
    Create,
    Read,
    Update,
    Delete;

    public static boolean isValid(String value) {
        for (CrudOperation operationType : values()) {
            if (operationType.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
