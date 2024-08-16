package com.elara.authorizationservice.enums;

public enum Language {

    English("en"),
    French("fr");

    String value;

    public String getValue(){
        return value;
    }

    Language(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        for (Language lang : values()) {
            if (lang.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
