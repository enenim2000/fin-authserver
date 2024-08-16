package com.elara.authorizationservice.auth;

import com.elara.authorizationservice.domain.SystemSetting;
import com.elara.authorizationservice.dto.response.ApprovalDependency;
import com.elara.authorizationservice.dto.response.ApprovalMail;
import com.elara.authorizationservice.dto.response.ApprovalPhone;
import com.elara.authorizationservice.enums.SystemSettingKeys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RequestUtil {

    private static final List<String> BILLS_PAYMENT_PERMISSIONS = new ArrayList<>() {{
        add("MpzYwKgaDXmRpqZQBMCzSzIagMUwQWv8go7Cg/TGYXg="); //Bills payment lookup
        add("gNwS/o8fRhJ//m0dBadEiOJRWn1cTQgAfVlKcug8Fzk="); //Get electricity discos
        add("Nt40QdTPK4gUpSrRhptoSnXo5YGxXNUnK7x2wJDzOQ8="); //Paybill
        add("jnME8v6RhLy1XWcgU740OyY9SxLjSxww0lCR+RDOlXY="); //Get Billers
        add("Ldn26aENFec1VEeNPb2l3Xkjg3UIMyPn8IxqnVV8mMo="); //Get Biller Packages
        add("JDRa024zXpnyrn7LfNLoDjQVhoHYFv+g4tKOgeCCIP0="); //Get Biller Category
    }};

    private static final List<String> LOANS_PERMISSIONS = new ArrayList<>() {{
        add("3Ql3cc5JswqVaqPmvtW2n+vXi4jyIZNX/07jy0fbQ3I=");//submit loan
    }};

    private static final List<String> TRANSFER_PERMISSIONS = new ArrayList<>() {{
        add("wYk8Zg/27uvY3F14G12lNvwoglclkh1GDURr4DRKnaY="); //Account statement
        add("wRi32FlY/FzmUnHIQzERcw9I7dex6OYGv0SbmXfmE4w="); //Get Outstanding Balance
        add("l5eysOWa7X4jN7QvaWmNl/HvSeT84LZhtQRPajRiqrY="); //Intra-bank transfer
        add("QcVXhQl+bWMr42OrKhtM979Ntq6LLExl3KdkK/xqPi8="); //Name enquiry inter bank
        add("oGtuVl0/eVQtcfUZXCFPCNFiZPGEa9NsP3gRQC8QF+E="); //Inter-bank transfer
        add("4/tCnenRy6nbtUcO/mmuCneLoTQpbAAQLkaAk1zRvJY="); //Intra-bank name enquiry
        add("HNtj+g7fwieuh1wsXn3Ax4GTn4g14qMYcnT8Yiy0Qyc="); //Get transactions
    }};

    private static final Map<String, List<String>> PERMISSIONS_MAP = new HashMap<>() {{
        put(SystemSettingKeys.BILLS_PAYMENT_SERVICE_STATE.getValue(), BILLS_PAYMENT_PERMISSIONS);
        put(SystemSettingKeys.TRANSFER_SERVICE_STATE.getValue(), TRANSFER_PERMISSIONS);
        put(SystemSettingKeys.LOAN_SERVICE_STATE.getValue(), LOANS_PERMISSIONS);
    }};

    public static Map<String, List<String>> getPermissionMaps() {
        return PERMISSIONS_MAP;
    }

    private static HttpServletRequest getRequest() {

        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }

    public static AuthToken getAuthToken() {
        AuthToken authToken = (AuthToken) getRequest().getAttribute("auth_token");
        return authToken == null ? new AuthToken() : authToken;
    }

    public static void setAuthToken(AuthToken authToken) {
        getRequest().setAttribute("auth_token", authToken);
    }

    public static String getChannel() {
        String channel = (String) getRequest().getAttribute("channel");
        return channel == null ? "" : channel.trim();
    }

    public static void setChannel(String channel) {
        getRequest().setAttribute("channel", channel);
    }

    public static String getToken() {
        return getRequest().getHeader("Authorization").replace("Bearer ", "");
    }

    public static String getClientId() {
        return getRequest().getHeader("client-id");
    }

    public static String getClientIp() {
        String remoteIp;
        remoteIp = getRequest().getHeader("X-FORWARDED-FOR");
        if (remoteIp == null || "".equals(remoteIp)) {
            remoteIp = getRequest().getRemoteAddr();
        }
        return remoteIp;
    }

    public static Map<String, SystemSetting> getSettings() {
        Map<String, SystemSetting> settings = (Map<String, SystemSetting>) getRequest().getAttribute("settings");
        return settings == null ? new HashMap<>() : settings;
    }

    public static void setSettings(Map<String, SystemSetting> settings) {
        getRequest().setAttribute("settings", settings);
    }

    public static String getMessage() {
        try {
            return (String) getRequest().getAttribute("message");
        } catch (Exception e) { //Done for non-request operation
            return null;
        }
    }

    public static void setMessage(String message) {
        try {
            getRequest().setAttribute("message", message);
        } catch (Exception e) { //Done for non-request operation

        }
    }

    public static String getApprovalMessage() {
        try {
            return (String) getRequest().getAttribute("approval-message");
        } catch (Exception e) { //Done for non-request operation
            return null;
        }
    }

    public static void setApprovalMessage(String message) {
        try {
            getRequest().setAttribute("approval-message", message);
        } catch (Exception e) { //Done for non-request operation

        }
    }

    public static void addDependency(ApprovalDependency dependency) {
        List<ApprovalDependency> approvalDependencies = (List<ApprovalDependency>) getRequest().getAttribute("approvalDependency");
        if (approvalDependencies == null) {
            approvalDependencies = new ArrayList<>();
        }
        approvalDependencies.add(dependency);
        getRequest().setAttribute("approvalDependency", approvalDependencies);
    }

    public static List<ApprovalDependency> getDependency() {
        List<ApprovalDependency> approvalDependencies = (List<ApprovalDependency>) getRequest().getAttribute("approvalDependency");
        if (approvalDependencies == null) {
            approvalDependencies = new ArrayList<>();
        }
        return approvalDependencies;
    }

    public static void addApprovalEmail(ApprovalMail approvalMail) {
        List<ApprovalMail> approvalMails = (List<ApprovalMail>) getRequest().getAttribute("approvalMail");
        if (approvalMails == null) {
            approvalMails = new ArrayList<>();
        }
        approvalMails.add(approvalMail);
        getRequest().setAttribute("approvalMail", approvalMails);
    }

    public static List<ApprovalMail> getApprovalEmail() {
        List<ApprovalMail> approvalMails = (List<ApprovalMail>) getRequest().getAttribute("approvalMail");
        if (approvalMails == null) {
            approvalMails = new ArrayList<>();
        }
        return approvalMails;
    }

    public static void addApprovalSms(ApprovalPhone approvalPhone) {
        List<ApprovalPhone> approvalSms = (List<ApprovalPhone>) getRequest().getAttribute("approvalSms");
        if (approvalSms == null) {
            approvalSms = new ArrayList<>();
        }
        approvalSms.add(approvalPhone);
        getRequest().setAttribute("approvalSms", approvalSms);
    }

    public static List<ApprovalPhone> getApprovalSms() {
        List<ApprovalPhone> approvalSms = (List<ApprovalPhone>) getRequest().getAttribute("approvalSms");
        if (approvalSms == null) {
            approvalSms = new ArrayList<>();
        }
        return approvalSms;
    }

    public static String getUserType() {
        try {
            return (String) getRequest().getAttribute("userType");
        } catch (Exception e) { //Done for non-request operation
            return null;
        }
    }

    public static void setUserType(String userType) {
        try {
            getRequest().setAttribute("userType", userType);
        } catch (Exception e) { //Done for non-request operation

        }
    }

}