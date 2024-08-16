package com.elara.authorizationservice.util;


import org.springframework.lang.Nullable;

import java.util.Random;

public class AppUtil {

    public static String generateOtp() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    public static boolean isEmpty(@Nullable Object obj) {
        return obj == null || "".equals(obj);
    }
}
