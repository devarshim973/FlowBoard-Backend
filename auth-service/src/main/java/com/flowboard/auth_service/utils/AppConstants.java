package com.flowboard.auth_service.utils;

public class AppConstants {
    public static final String page = "0";

    public static final String size = "10";

    public static final String sortBy = "userId";

    public static final String direction = "asc";

    /* Otp limit for a single day */
    public static final int otpLimit = 5;

    /* Cooldown before a new OTP can be requested */
    public static final int otpResendCooldownSeconds = 10;
}
