package com.elara.authorizationservice.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PermissionUtil {

    private static Map<String, String> CUSTOMER_PERMISSIONS_MAP = null;

    private static final List<String> CUSTOMER_GROUP_PERMISSIONS = new ArrayList<>(){{
        add("607Fzo1hvhzs7avLG8uxpUrrrdV5BtSEklXgW7BVnpw="); //RESEND_PHONE_OTP
        add("RrY7bsjRomSRsQlHPFo6yBmLLkARlbCQwfpBbDhwJmA="); //CHANGE_PASSWORD
        add("T0vxmgYagDvEPFyU0LSGEEsZdl/zgDZlWrOOaZhYfi0="); //VERIFY_REGISTERED_PHONE
        add("5woeZ8SeqExN+QIWTC6Y02KiYwUIzrE/eH1YOtXschY="); //VERIFY_REGISTERED_EMAIL
        add("CL8PiqvquAXCjQhQipwi9gNOwxbsqXIev2TmfGoYOYY="); //RESET_PASSWORD
        add("2kvbrv0oKuNjmMAySJQJ6LM5EdiIp9fH23YQG9+b8cc="); //RESEND_EMAIL_OTP
        add("XIQWLnShRztkyWmChT5pGWjQS5fQ7KZDR/0I3PB8fzc="); //RESET_PASSWORD_INITIATE
        add("KmdSjmFfJzLMwczfmVuOd+SBwxaBDH6/nNtIRFYIcWI="); //CREATE_NEW_CUSTOMER
        add("ce4NyPkOxLT+LVClOhf6vfvA4I6MleF2corXKD5sKn8="); //VIEW_LOAN_REQUIREMENTS
        add("ffAvo2EG6tO2N5J/ZxRooppelYWbGrwG/vu06xVsoFE="); //SAVE_LOAN_APPLICATION
        add("3Ql3cc5JswqVaqPmvtW2n+vXi4jyIZNX/07jy0fbQ3I="); //SUBMIT_LOAN_APPLICATION
        add("4Sp1rKqagU54vJVzCGgXvOZiMancLVEb1o2rV0bH6Is="); //VERIFY_BVN
        add("pxVuWjqY36PK5gtxdvYAcgG3ItRUa4Fi/iRBl7PM/8Y="); //GET_CUSTOMER_DETAILS
        add("kaeRTn7XnNJWTKhZhEyzhCm/qVdsNC7oAKx0ZvClGWA="); //INITIATE_BVN_VALIDATION
        add("EF+44jnNSg0zf7Isqu5JfSyU3dq4tJJgykp3PlZCxm0="); //ONBOARD_EXISTING_CUSTOMER
        add("wRi32FlY/FzmUnHIQzERcw9I7dex6OYGv0SbmXfmE4w="); //GET_OUTSTANDING_BALANCE
        add("bwIdsgBG7cB4XnwkiyBbxoRH696ZExOsTcjqV19lZNA="); //GET_ACCOUNT_BY_CUSTOMER_ID
        add("H6hcpE6Xq8+29On6A4g7HiD/nnHOAag0qNsIXy5ger0="); //GET_CUSTOMER_NAME_BY_ACCOUNT_NUMBER
        add("WNwEz6AxazjyYf0YoqW+LQPc0zsBKPiHL/I2vCaabbc="); //GET_ACCOUNT_BY_ACCOUNT_NUMBER
        add("UidBJrwidpjTv4EmJjcCRl478tF9QSyN4Ize1lwbMl8="); //CREATE_ADDITIONAL_ACCOUNT
        add("2/104m/qbABuwwxJ2M3rYwE2WdAtdorybyQss2XL8Qk="); //GET_USER_DETAILS
        add("HTMpP2nmdmj2dMVMH/ZzNSCPvLQzuIFLx8PdMMjaxn0="); //SET_PIN
        add("kdTrB5qClPLjLsLbihVihb943PFftG/cSxNzAhS16so="); //GET_AVAILABLE_ACCOUNTS_TYPE
        add("WwOLVWkg7s6Zfpz6GZn7hVARFwKxINXEqSSFPxY9Gy8="); //REMOVE_BENEFICIARY
        add("VBeZoKeezQYtEZrTbhSxQu3IIuqah5nlPkhlgEFLPIw="); //GET_BENEFICIARIES
        add("57S8Bu0dFfVbXX3rXJNINCCdCBr+IQJffU0NWZmoKPg="); //SAVE_BENEFICIARY
        add("l5eysOWa7X4jN7QvaWmNl/HvSeT84LZhtQRPajRiqrY="); //DO_INTRA_BANK_FUNDS_TRANSFER
        add("QcVXhQl+bWMr42OrKhtM979Ntq6LLExl3KdkK/xqPi8="); //DO_NAME_ENQUIRY_INTER_BANK
        add("nszv61R/Vxjb4eKddGL5i5PCe1Tuw7wx4+E1+FrocnY="); //CONFIRM_BVN_VERIFICATION
        add("KkSGdDvbWjw2+pQgzbVxxTb9h6Ke2geQcs2ZDHnKkhc="); //GET_COMMERCIAL_BANKS_DETAILS
        add("oGtuVl0/eVQtcfUZXCFPCNFiZPGEa9NsP3gRQC8QF+E="); //DO_INTER_BANK_FUNDS_TRANSFER
        add("4/tCnenRy6nbtUcO/mmuCneLoTQpbAAQLkaAk1zRvJY="); //DO_NAME_ENQUIRY_INTRA_BANK
        add("HNtj+g7fwieuh1wsXn3Ax4GTn4g14qMYcnT8Yiy0Qyc="); //GET_TRANSACTIONS
        add("pXLp4VXy/Z+8Hx6f308lUpSQxfe6k9qf3TuscZRpi44="); //GET_SINGLE_TRANSACTION
        add("nyNebziYzomi3y1+sEoIpP6z9jFMQXpFVMZHLrcdRFs="); //GET_COUNTRIES
        add("ljFgGy+oHU7KoOZ+Pb4/yougn/7KKENJdnFMI0SDBfg="); //GET DOCUMENT TYPES
        add("MpzYwKgaDXmRpqZQBMCzSzIagMUwQWv8go7Cg/TGYXg="); //BILLS_PAYMENT_LOOKUP
        add("+NV93wcd0N15eBhHvqX3yaU3ncxPl/MOa4LUtzrIjQw="); //GET_ACTIVE_PROVIDER
        add("gNwS/o8fRhJ//m0dBadEiOJRWn1cTQgAfVlKcug8Fzk="); //GET_ELECTRICITY_DISCOS
        add("jnME8v6RhLy1XWcgU740OyY9SxLjSxww0lCR+RDOlXY="); //GET_BILLERS
        add("Ldn26aENFec1VEeNPb2l3Xkjg3UIMyPn8IxqnVV8mMo="); //GET_BILLER_PACKAGES
        add("JDRa024zXpnyrn7LfNLoDjQVhoHYFv+g4tKOgeCCIP0="); //GET_BILLER_CATEGORIES
        add("PXy91hKGFyforFehcGKSvKVRjozZSo5n94jxa1lhiNo="); //UPDATE CUSTOMER KYC DETAILS
        add("s/34pNxp3REOXJfdxb5vjZUadnIUl0+5hlYpdCYGRj4="); //SAVE CUSTOMER KYC DETAILS
        add("gJzwoNoOmsEea0Ja9vexedwQijcD1WmYKc0odYa7pwc="); //GET CUSTOMER KYC DETAILS
        add("2ZvymZYZO9EiLGLBaM6PZHjn+EJyz+cKyx6xlwRRFgU="); //GET PREFERRED MODE OF CONTACT
        add("fbhS9nUdKxGSVQQNsb/hz13rX3lZkjeJafHOeTTPYaU="); //GET TITLES
        add("wBBQCHc4yzIj19GbE+NcTHb7VO9j2ZKQfdDoMSX9dAU="); //GET EMPLOYMENT NATURE
        add("65ulDEbZ7ddBE0CJEqTZcL7ekMcd0uWFQ4avgBE3/R8="); //GET MARITAL STATUS
        add("OMGwofh8Ig7KPAgvLo73cI27Oj+TBr2f+dKAf8DiS34="); //GET RELATIONSHIP
        add("CIHDoqoa5m9QRO10z3O3KFaSExkcxyIhPVcJxbdALG4="); //UPDATE_BENEFICIARY
        add("ZFWMzruXx7744HIyJzQ8fcaNrqMEUluVLfb7rYvORE0="); //DELETE_BENEFICIARY
        add("qDqRDlWsZ69wTyFsrItZ3dcP7zWCxjQpFRSnROYuceI="); //GET_BENEFICIARY
        add("XM/AbQfu0V0KL5Okm982SZC5Ls7Tpl//BfWfKK9lfyw="); //CREATE_BENEFICIARY
        add("Nt40QdTPK4gUpSrRhptoSnXo5YGxXNUnK7x2wJDzOQ8="); //PAY_BILL
        add("Jcx8I6h+BpeQCpU8uv5K3A4LTLMiPQiWSLvaoqnyxb0="); //RESET_PIN
        add("IbsgwWWbdztjmu+m9gu6gSIor3luzfbCKLcvrIFNwbI="); //CHECK_PIN_SET_STATUS
        add("dmY+fN1ZhyXGb5+W6y7aoApzZOBd7UTbfx5s0J+4FtA="); //BLOCK_TRANSACTION_PIN
        add("8wabUj4j42H6RJLgKNvfay8noI37TjyU0PLEVbgtZvo="); //GET_ALL_USERS
        add("JaZvFZzSGa46rn4FJ80rOfet/nvyNdm98nt4L5BIXDo="); //CHANGE_PIN
        add("NUOdM41cflrtzZQsYM6wxGNaJmreyPh4ehzTS449C84="); //UNBLOCK_TRANSACTION_PIN
        add("GR7Tj6whgt1nUlW82lb/7qTGOVNOb6Qc8RsgXmhhvcc="); //CREATE_COMPLAINT
        add("500ldynVoG4ttAhtSQ+P+tka99hLN2Fiouy9ZmWHLIQ="); //GET_COMPLAINT_TYPES
        add("/unDG5cBzKcywMJgwtMao0Zqd/sKpOA7bzdiEsZWIqY="); //VIEW_COMPLAINTS_BY_STATUS
        add("kbvalsQwKj9whFERZgnfsFWLePKfuCiryJ6mHq9SWaI="); //VIEW_ALL_COMPLAINTS
        add("m3cBwB4Pu55p4qi9aV4JAi4QXn4V6iF57hoyhTgg7uo="); //GET_COMPLAINT_STATUSES
        add("rc1+C0dZi6M09IGlPSkDBEVHf+5BB2Ar8h68+cXq1/A="); //VIEW_SINGLE_COMPLAINT
        add("VnF5jzfqh3crXUZiTDroZD5WhKgJ3iVClRELXmhqpOI="); //VIEW_COMPLAINTS_BY_TYPE
        add("D7hjHA28dWTjVIPG59FzOBsx+VqHKRZwLJ3jR31wEXI="); //UPDATE_COMPLAINT_STATUS
        add("8lkZpqK2fdcWSsdvRgsLMCllm7/OqnO4iE/iSnIZlxk="); //VIEW_LOAN_PROCESSED_COUNT
        add("/YHpj/OBoJ8hQCuXNVz7phS4YH1RNP2zpdiQwgu+EMo="); //RESOLVE_COMPLAINT
        add("/WVuxiXtndwKoWQ7Eywg7kpw77i8VQChDUdre4LFWsE="); //VIEW_ALL_COMPLAINTS_BY_DATE_RANGE
        add("wYk8Zg/27uvY3F14G12lNvwoglclkh1GDURr4DRKnaY="); //GENERATE_ACCOUNT_STATEMENT
        add("EaJBPyU6po4W5uH+l0Rw5MZG2azeyPtkScnkEAdPs58="); //CREATE_CUSTOMER_PROFILE
        add("qozOZwnGVhP2omL8bfpxOmu5g8Svp+lR9VsrS1BhMWU="); //SEND_OTP_FOR_ONBOARDING_EXISTING_CUSTOMER
        add("J+gAiPpbdZeec+eQrS/9jEZXY8cykU4RdZB1l8uUHyg="); //GET_TRANSACTION_BY_REFERENCE_NUMBER
        add("Tu3JyXdOyqRdRrnZ3VJQ4ieQO2vPQ5Uz0waPUVZha8s="); //SAVE_PROFILE_IMAGE
        add("O9qbQJzRItYNlZNEAWdAzA1VNixbqEqbtgEjznJhupA="); //UPDATE_PROFILE_IMAGE
        add("Ku7JTucmv4Xzi1KcoS6bi5HeWCkLmqhYaQO/8PKDuLM="); //GET_TRANSACTION_DETAILS_BETWEEN_DATES
        add("TRYGgHAmeiTtE1s3Mool+S1KdXCL6WQqrHfSHzsTrvc="); //GET_TOTAL_AMOUNT_IN-TRANSACTIONS
        add("Nk571uPLwJJQBrAgc7WwJ4KeNbi/JKQkRmyvPrSNa2M="); //GET_TRANSACTION_STATUS
        add("29JZB1GRuuAPm4s6aNGymwIqh2uaCmEmZJtCAjfWotk="); //GET_TRANSACTION_TYPES
        add("wHVOgL1oynwh4LVvnFy4Tt9BruxI5uA7abyNUv0LGWo="); //GET_CUSTOMER_COMPLAINTS
        add("Rn6XxgKqXvpxVpWMEJZER2OaHj54tCwk6jgnR2TljP0="); //SEND_OTP_PHONE_NUMBER
        add("jJ4yjWVHS90vmKmbOBApFrViQriLCrROCB4l745Zh9s="); //VERIFY_OTP
        add("b9gJBNOzX+8UcFG3UfqC0uEyNBh3wXZaF+8LRiB/3VE="); //GET_TRANSACTION_BY_REFERENCE_NO
        add("FgWnOc/znUlriy6VDf83rHeRehmEnkpisn5KE2XgttI="); //CREATE_FIXED_DEPOSIT_ACCOUNT
        add("2U2OuLz521xb54oeZI2uPKIzbQLGauWAKh91eCPWx8c="); //GET_FIXED_DEPOSIT_ACCOUNT_DETAILS
        add("cd4/nGAiGRYCg0pV+vMrG23BYCHiJ4wzcHM+OAr2m0k="); //GET_FIXED_DEPOSIT_ACCURED_INTEREST
        add("O1+gd/UFxLnLFKHl3tr4YvmpcRFXWqfb/j21ovwyDq8="); //LIQUIDATE_FIXED_DEPOSIT_ACCOUNT
        add("i/Hvz+J/QetV3o3B6HTx7s2T4c/pSxPbfq5pIl8gJiw="); //GET_CUSTOMER_KYC_INFORMATION
        add("zuhZuwzyy+iuI/gVZQqrFp9w4jJ/RzGzshyjGsQtMeY="); //GET_CUSTOMER_BY_KYC_STATUS
        add("XFbb/UbP/nd3Q64t2I9uSJEHHvVevUteig4RZJXteJ4="); //GET_AVAILABLE_FIXED_DEPOSITS
        add("EFcupa/8W/7lFXG6ACOheMNPfi5/iGK79LWR4w6Mifc="); //DELETE_PROFILE_INITIATE
        add("E1BAZhtGDipIqnrjmT02fwAQZugKrjYwyOQyZo8T1dI="); //DELETE_PROFILE
    }};

    public static final List<String> COMMON_DEFAULT_PERMISSIONS = new ArrayList<>(){{
        add("607Fzo1hvhzs7avLG8uxpUrrrdV5BtSEklXgW7BVnpw="); //RESEND_PHONE_OTP
        add("RrY7bsjRomSRsQlHPFo6yBmLLkARlbCQwfpBbDhwJmA="); //CHANGE_PASSWORD
        add("T0vxmgYagDvEPFyU0LSGEEsZdl/zgDZlWrOOaZhYfi0="); //VERIFY_REGISTERED_PHONE
        add("5woeZ8SeqExN+QIWTC6Y02KiYwUIzrE/eH1YOtXschY="); //VERIFY_REGISTERED_EMAIL
        add("CL8PiqvquAXCjQhQipwi9gNOwxbsqXIev2TmfGoYOYY="); //RESET_PASSWORD
        add("2kvbrv0oKuNjmMAySJQJ6LM5EdiIp9fH23YQG9+b8cc="); //RESEND_EMAIL_OTP
        add("XIQWLnShRztkyWmChT5pGWjQS5fQ7KZDR/0I3PB8fzc="); //RESET_PASSWORD_INITIATE
        add("Rn6XxgKqXvpxVpWMEJZER2OaHj54tCwk6jgnR2TljP0="); //SEND_OTP_PHONE_NUMBER
        add("jJ4yjWVHS90vmKmbOBApFrViQriLCrROCB4l745Zh9s="); //VERIFY_OTP
        add("VfrXleaGn3T95CJCXw3p36lDVehKSLl5Wa4M+dS6NIE="); //VIEW_DASHBOARD_REPORT

        add("t00VKpUbzIW/KkIsv9FI4u7Am+IE7kZJH4dhN1/NzDA="); //VIEW_ALL_LOANS
        add("Fz1KkqbnzPuzhJUBDdTGgKyXKDHHzxlmw5KHGBPYTHw="); //VIEW_LOAN_REPAYMENT_SCHEDULE
        add("Wkkh+nwrQ0A9b+rcPzMwJRcEmLsSqYLeTG5iypNvwl0="); //VIEW_USER_ACCOUNTS
        add("D4BuF09z/etgVY8rwd52K2OQvX7rt27gbQkzAjs9Pls="); //VIEW_LOAN_DETAILS
        add("ce4NyPkOxLT+LVClOhf6vfvA4I6MleF2corXKD5sKn8="); //VIEW_LOAN_REQUIREMENTS
        add("xVYCJnlAu4+rYahrISisGlYspirdt5K1EVAlvd/ftjY="); //VIEW_LOAN_REQUIREMENT_DETAIL
        add("HJXhjJzurEVfh8J5Y3ek5SMPP1aESu9FfoimK7GKB04="); //REMOVE_USER_KYC_DETAILS
        add("69tJQyQysxADHV5aB8vIF9S5X8DwiFOqnVzHmYHRtRw="); //UPLOAD_USER_KYC_DETAILS
        add("zQWJMK0iGDwfXVJFfbw0pMnEIJFKTCcsnbijDXtNYmo="); //GET_USER_KYC_DETAILS
        add("Ouq0PbMfnr9XfpSjRQC6Tun8koka5cCkyP1R9weIpR4="); //CHECK KYC VERIFIED STATUS
        add("7mpPAFZ2F5oVHdXVxDj+O1LtK+BxbnSWuOGVnrHMNsw="); //ENABLE_DISABLE_USER
    }};

    public static final List<String> EXCLUDED_PERMISSIONS = new ArrayList<>(){{
        add("pKYAchNmjMzpPUVPEzWFRVZdO93jDvwxw1O/N6QqGso="); //UPDATE_APP
        add("EBDGSUOkiKtw324pUQ/GKK1re9v4PGkaahQ41+1HATU="); //UPDATE_COMPANY
        add("PVWsQ862KajwfDvTZ81HMSkFXQxj40FuSztrTEzdwmE="); //CREATE_APP
        add("y44PqCl2euLU7s34MKeZztVMij8U/qbOV+PgXU4QJHw="); //CREATE_COMPANY
        add("nyNebziYzomi3y1+sEoIpP6z9jFMQXpFVMZHLrcdRFs="); //GET_COUNTRIES
    }};

    public static Map<String, String> getCustomerPermissionMap() {
        if (CUSTOMER_PERMISSIONS_MAP == null) {
            CUSTOMER_PERMISSIONS_MAP = new HashMap<>();
            for (String permissionId : CUSTOMER_GROUP_PERMISSIONS) {
                CUSTOMER_PERMISSIONS_MAP.put(permissionId, permissionId);
            }
        }
        return CUSTOMER_PERMISSIONS_MAP;
    }
}
