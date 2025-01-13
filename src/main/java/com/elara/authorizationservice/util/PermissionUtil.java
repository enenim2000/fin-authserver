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
        add("R6rZhd4QbOl+ig9xBrEgUYDFuDVxYt7Kpt9jclJ87I4="); //CREATE_NEW_CUSTOMER
        add("tEncwTh6ReJf5RpdhzToyYh2zlnwi98mzl/nH/HJBXQ="); //VIEW_LOAN_REQUIREMENTS
        add("6Fl3gwz2+Jsj6dB1VX4OF13nWczVi7iw9dRd2DCU/qc="); //SAVE_LOAN_APPLICATION
        add("yVrG5s58+eC3VlO5F33pyE3U6gikHpe8SLbUiKxQpeg="); //SUBMIT_LOAN_APPLICATION
        add("9+9bKzlsDsJ+93lZLPUbr3qzbObC+Sb0bfdhOsJEp1c="); //VERIFY_BVN
        add("z2bfN2N/fZ0HtyKbQQtgYeJzUkk4+MW5COOLGbNKkHM="); //GET_CUSTOMER_DETAILS
        add("5dlMFvzj87ip/fuUySSUwkfHe7pN9fkc94gp1OKZHJI="); //INITIATE_BVN_VALIDATION
        add("3D48YzPZNk8c7SkOm9E/cmfigEcUJ2W1X2XTsPmsXb4="); //ONBOARD_EXISTING_CUSTOMER
        add("jyxc7Tnzzr9CgwjmCQQzsyI0PaDj3Bvq9k/qWq1Nt5w="); //GET_OUTSTANDING_BALANCE
        add("bwIdsgBG7cB4XnwkiyBbxoRH696ZExOsTcjqV19lZNA="); //GET_ACCOUNT_BY_CUSTOMER_ID
        add("tLNcuFpdBR6E1b/FWMRUPO7Evk8i1XBA5r/HbLMCwgs="); //GET_CUSTOMER_NAME_BY_ACCOUNT_NUMBER
        add("bq+HuwsEn/qfru7DqPw15Q40A0X/tTxMixh8GpRDXzk="); //GET_ACCOUNT_BY_ACCOUNT_NUMBER
        add("iYRWIDGT8dUjCVnMTUbrfLtq8qpqNH+Z3BzdxiK5Ebs="); //CREATE_ADDITIONAL_ACCOUNT
        add("QWiMcpdbh3SaBQclPSQgfDcr3cgSp9rvW9m/ci7WaAQ="); //GET_USER_DETAILS
        add("adLYLf2WhPiQ3hEPSzunOhSQJaqTKgHBiocd7vnzSdY="); //SET_PIN
        add("bq+HuwsEn/qfru7DqPw15Q40A0X/tTxMixh8GpRDXzk="); //GET_AVAILABLE_ACCOUNTS_TYPE
        add("WwOLVWkg7s6Zfpz6GZn7hVARFwKxINXEqSSFPxY9Gy8="); //REMOVE_BENEFICIARY
        add("ifEp60TUxXajpFNIj6OjprDHEuhRvinLJSfJAqBQX9w="); //GET_BENEFICIARIES
        add("57S8Bu0dFfVbXX3rXJNINCCdCBr+IQJffU0NWZmoKPg="); //SAVE_BENEFICIARY
        add("LYVIj8kTY3g4hOxVsP/B0Tt3OHmcMaqnEkbc+mBOK/0="); //DO_INTRA_BANK_FUNDS_TRANSFER
        add("pIE78tNcJmlvc+ZntokBIC9sUbEkIVYjK66SqQKILII="); //DO_NAME_ENQUIRY_INTER_BANK
        add("nv/pk/VI+0rtCHkqRzKWNSOmvdVMU3+o2m//GknJ3RQ="); //CONFIRM_BVN_VERIFICATION
        add("qWyU6TGOu3FB9g0/lQRf/PmFJDni3FkAu37y2fSAXIA="); //GET_COMMERCIAL_BANKS_DETAILS
        add("aQm8a+98xlejlL9a5gdZ9pUMSbtHwPrh0z/Ds5xlPgE="); //DO_INTER_BANK_FUNDS_TRANSFER
        add("zVDKh/MSp4arLOXlypnx9op18viCIgm+hyoo8wKLfQs="); //DO_NAME_ENQUIRY_INTRA_BANK
        add("qQ9kT7KsdkorpMmbL89eGiUwDwcEJVLT4d9+UQ2Wn/Q="); //GET_TRANSACTIONS
        add("pXLp4VXy/Z+8Hx6f308lUpSQxfe6k9qf3TuscZRpi44="); //GET_SINGLE_TRANSACTION
        add("BjplGUmGAyBod/77tSFWwrWzR2JY2RXNsWzxwM+fX+M="); //GET_COUNTRIES
        add("DjD4tNzyERG8YeTkEvPASA0saT8r7vYNlmVTvEZJxVc="); //GET DOCUMENT TYPES
        add("Zyq/kWSD7J/Gn6Wx7ZE3yIbSf8BKF7Psuclb8WCMncU="); //BILLS_PAYMENT_LOOKUP
        add("qIuYXurNl4Bu5AHQ2MnqB8Z6a5Bh2JFnDDrjAFeYEQI="); //GET_ACTIVE_PROVIDER
        add("gNwS/o8fRhJ//m0dBadEiOJRWn1cTQgAfVlKcug8Fzk="); //GET_ELECTRICITY_DISCOS
        add("d2n4KyW7vGjoApnVYo+/HLow88jrBVOeX16+sqC4qrU="); //GET_BILLERS
        add("aXSX56c/x3IY15aB+jF6VARe6IBbv5bABtXJfFiLPt0="); //GET_BILLER_PACKAGES
        add("Mr6d3UJ1tZTj8+UhY7fCiN/ZpqyVvprPNMM7FJueTyQ="); //GET_BILLER_CATEGORIES
        add("3rpz7cjv7sfHhz48lMG3N26RhLGLl+L+B4czIObwMIs="); //UPDATE CUSTOMER KYC DETAILS
        add("6tQo2bOsDCMd4aDfx36w8eeehLC0dg9BmEOtFJ1qXxo="); //SAVE CUSTOMER KYC DETAILS
        add("HH1sSwDB5ZbOF61Zi4J1RPIo/IG4V00YGRK1D/HOPcE="); //GET CUSTOMER KYC DETAILS
        add("oQW1Zd3N6bSfi3aLOn+9rwCSKTSAZEQYzusKFf8HzMM="); //GET PREFERRED MODE OF CONTACT
        add("vOpmUNBDQSskQmvKV6by9sP12MLo5TYdF3tAgDnM1x0="); //GET TITLES
        add("HEAFQypOdJA+boLKqEI0BAoogNEkC6J8N8IRG4btmNI="); //GET EMPLOYMENT NATURE
        add("xIIY0nTSiIjVSARn6nMKtvNyrHiKaLTrcXgbPznBYLw="); //GET MARITAL STATUS
        add("Wd7rlfuMe6pp6rsXbin5TI8TtQDJ30NmJLZX41HK4lI="); //GET RELATIONSHIP
        add("RV/XkNtaz5cz5uJIhbzaKXE1Yi43T96FvKDt7nHqizI="); //UPDATE_BENEFICIARY
        add("cluWNR6mRVRbJId/bMlSkXkLQlRY3UJW5c0tlWNRlrs="); //DELETE_BENEFICIARY
        add("ifEp60TUxXajpFNIj6OjprDHEuhRvinLJSfJAqBQX9w="); //GET_BENEFICIARY
        add("oGuWqsp48Y8Ll7F7332L4TWToAi2PFpo8tU3YohSAmQ="); //CREATE_BENEFICIARY
        add("M+LUBQeoQxKWjx2O05KG/z55539qE50mD2Wp5hA1DOs="); //PAY_BILL
        add("nPDeIJh3wrTHQ3WWgOFQ07cjwoMLFFfnooCuxWStEzE="); //RESET_PIN
        add("eoId22vFk80sD2QPXOrWUJ1+tOUoT3pYfdAgAbBaYL0="); //CHECK_PIN_SET_STATUS
        add("hPs1/6nZ9q+3POCWict5VnxU0QahNwMTODP6Ew8ldQk="); //BLOCK_TRANSACTION_PIN
        add("44Taf3fM3pGnw5kJ4Tgvoeb9RUBg0T77p9GY1iYPB9g="); //GET_ALL_USERS
        add("4nlg2yoOj1XPK2Ozk2JvOQC8Cuo8lGARZ0/+DA200Do="); //CHANGE_PIN
        add("VSKXoI1j/Apvn4puY5ghuD1xI0q9hm8poN3CKlURQV4="); //UNBLOCK_TRANSACTION_PIN
        add("oSM+eU0MvTj7D9lkkrBQnyQqtR15p51fuNTBLtdthfA="); //CREATE_COMPLAINT
        add("500ldynVoG4ttAhtSQ+P+tka99hLN2Fiouy9ZmWHLIQ="); //GET_COMPLAINT_TYPES
        add("TW9WehnNNSnENO2RmD6jrFwXTxwum+PlFJnNU7DdiXw="); //VIEW_COMPLAINTS_BY_STATUS
        add("V5zA7BGgcnNx3zgmpKwuisj0CzFUYW+ipBx568v3q30="); //VIEW_ALL_COMPLAINTS
        add("VnzHRwgjVq5+adFQYA6XlQZwuQHwLw3SDwhKaioiYfU="); //GET_COMPLAINT_STATUSES
        add("tBUOXKOBNSzeeGDv2gksSjWYPHemn//E6+A2s3NZwL0="); //VIEW_SINGLE_COMPLAINT
        add("D5ZWGUWAfK/AkYwC5ikDE1kKu6IQjQdcGRRa+ktai9k="); //VIEW_COMPLAINTS_BY_TYPE
        add("QFD6dgJo5/sZs87KAXfiq3j+P8OoTfqyxZgUe8bEnFs="); //UPDATE_COMPLAINT_STATUS
        add("zJj7oUX4o/2Jv6u9dSEhBdKtXcYFXcTmTP2vrwVlcBw="); //VIEW_LOAN_PROCESSED_COUNT
        add("ItEk6OX6T78r2dGQwVpcde/t+ZJTbHUpZ7hcmApJNLs="); //RESOLVE_COMPLAINT
        add("jhUwyK3v5i9Je+YA2JCLrjZoN/7N8xduMefqLv8MAqw="); //VIEW_ALL_COMPLAINTS_BY_DATE_RANGE
        add("tnc4LeFLKc+bUOdrt5VB7wptxkawxlszb5XXM2F+Yus="); //GENERATE_ACCOUNT_STATEMENT
        add("kwjoP8OEZHBF9EfFoFnEEtDv+X4RzBeTE/nUKuCHDJs="); //CREATE_CUSTOMER_PROFILE
        add("obsUsjr5c3XWx4LT4KEexvBhJYvYPg0omN4vOQ5Ye4w="); //SEND_OTP_FOR_ONBOARDING_EXISTING_CUSTOMER
        add("HJ+50aqNsnzBPbROkUFEM8wK2ubLYYMiNHN7Ern3OV8="); //GET_TRANSACTION_BY_REFERENCE_NUMBER
        add("TAwYWTiSdfKaQ6ZNXoKPUMnaciMQN4Im/ZrxHP8KnP4="); //SAVE_PROFILE_IMAGE
        add("Bdk8hExLFVWy94qs5FBZV78fTaCMIVe+t/bUQu4bjbE="); //UPDATE_PROFILE_IMAGE
        add("7Oh0DoHUfb5HOrZNAf08JnFTXk1XCOImIkmhub9bBUI="); //GET_TRANSACTION_DETAILS_BETWEEN_DATES
        add("ntnZXDZJurcZ3aaWefPNcgKNaH6DAVWHZ9VR2RvmF9Y="); //GET_TOTAL_AMOUNT_IN-TRANSACTIONS
        add("4O7cbBseMcR2uWmD1xC3d33MLRXGhtGvEhi6fBnnYrs="); //GET_TRANSACTION_STATUS
        add("oW3XIJtlmi/hO6g3A5w+k+LM+0rdvPGuQn/Sul5e7ds="); //GET_TRANSACTION_TYPES
        add("AsxKbPw8Bo+2MnzyEqUMYKDAluwS5MHky+1nXvjJIWg="); //GET_CUSTOMER_COMPLAINTS
        add("dS0lU1T+x5l9KAoVuWxQwDSArpvY3u00iq04iIpYUD4="); //SEND_OTP_PHONE_NUMBER
        add("jJ4yjWVHS90vmKmbOBApFrViQriLCrROCB4l745Zh9s="); //VERIFY_OTP
        add("NvvqR8JaTs8XjNGlyw73T8zSozXe6iAGpGCMD8IRPaE="); //GET_TRANSACTION_BY_REFERENCE_NO
        add("kWRWZGpLLoO21veazR2uzoS3r2Thxbp55uwMwZya4Ts="); //CREATE_FIXED_DEPOSIT_ACCOUNT
        add("JPdHPyoUSvn4Mfu4pFuy2GhfwAeEtA2RnjibTN5s0Fw="); //GET_FIXED_DEPOSIT_ACCOUNT_DETAILS
        add("hp1bxjR2yjfI+dvk1oFPvE/vO7RMYncaNt6LAB7j1x0="); //GET_FIXED_DEPOSIT_ACCURED_INTEREST
        add("l3e2osC/GQbZe2PajZeM0q1L7Lm7dpU14AWbjVqmbwA="); //LIQUIDATE_FIXED_DEPOSIT_ACCOUNT
        add("UoZPIRjdCbcM42YQEami7aRNh34EhzoAWoW92ZtEONg="); //GET_CUSTOMER_KYC_INFORMATION
        add("vuABXyzaxDaqF45pF0FKVLXXM9LU8EYsgI3pM8n3Xmw="); //GET_CUSTOMER_BY_KYC_STATUS
        add("zGedx6efvMvR0UlNhCxryoDU+fYFk3LH4wSkk5EZjgE="); //GET_AVAILABLE_FIXED_DEPOSITS
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
        add("dS0lU1T+x5l9KAoVuWxQwDSArpvY3u00iq04iIpYUD4="); //SEND_OTP_PHONE_NUMBER
        add("jJ4yjWVHS90vmKmbOBApFrViQriLCrROCB4l745Zh9s="); //VERIFY_OTP
        add("Kjy0uv9FFkEE/SGzRJ07D9JFMPOnCljidt6N60b6+PY="); //VIEW_DASHBOARD_REPORT

        add("ewG4988NeKVINUk+qEpC5mhOT4h8b3kbZY4c3XdzpsM="); //VIEW_ALL_LOANS
        add("6LGWdnsWItBDD/4u1P1x9RqyzN6Iu9JtVaSSbubzf4c="); //VIEW_LOAN_REPAYMENT_SCHEDULE
        add("icxkKO/1Ps1R0Jt+MPiWOKyDq/PCylSC4K66+RJvbM0="); //VIEW_USER_ACCOUNTS
        add("KPqLsvSDcyNlcH5ZWX4x5gtnUx5HEVylA9hXhjA2SHg="); //VIEW_LOAN_DETAILS
        add("tEncwTh6ReJf5RpdhzToyYh2zlnwi98mzl/nH/HJBXQ="); //VIEW_LOAN_REQUIREMENTS
        add("EXbyUwgnar8dMXPgn1+sTodaRZRtcghF5tTA5OtHkqU="); //VIEW_LOAN_REQUIREMENT_DETAIL
        add("iqxJRFuF6VQVvThLLMFapMFNbf8FXoqpRpbdm54IhV0="); //REMOVE_USER_KYC_DETAILS
        add("601Qwr13xLR91iFJ9nSUISDkV0/NXhGMf9ubtWZkcys="); //UPLOAD_USER_KYC_DETAILS
        add("4uMw1GV3LhqOdyr5HnFIFx3L24PV0xB2ZMP+gwtR7CQ="); //GET_USER_KYC_DETAILS
        add("EAcgwPBW9t7QGUkmrKvKv4qi21uQqYv2aafZyMPz7SQ="); //CHECK KYC VERIFIED STATUS
        add("7mpPAFZ2F5oVHdXVxDj+O1LtK+BxbnSWuOGVnrHMNsw="); //ENABLE_DISABLE_USER
    }};

    public static final List<String> EXCLUDED_PERMISSIONS = new ArrayList<>(){{
        add("pKYAchNmjMzpPUVPEzWFRVZdO93jDvwxw1O/N6QqGso="); //UPDATE_APP
        add("EBDGSUOkiKtw324pUQ/GKK1re9v4PGkaahQ41+1HATU="); //UPDATE_COMPANY
        add("PVWsQ862KajwfDvTZ81HMSkFXQxj40FuSztrTEzdwmE="); //CREATE_APP
        add("y44PqCl2euLU7s34MKeZztVMij8U/qbOV+PgXU4QJHw="); //CREATE_COMPANY
        add("BjplGUmGAyBod/77tSFWwrWzR2JY2RXNsWzxwM+fX+M="); //GET_COUNTRIES
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
