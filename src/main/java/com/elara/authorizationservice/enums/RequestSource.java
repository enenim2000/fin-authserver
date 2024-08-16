package com.elara.authorizationservice.enums;

public enum RequestSource {

    PlatformBackend("platform-backend"),
    Mobile("mobile"),
    Service("Service");

    String value;

    public String getValue(){
        return value;
    }

    RequestSource(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        for (RequestSource lang : values()) {
            if (lang.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
