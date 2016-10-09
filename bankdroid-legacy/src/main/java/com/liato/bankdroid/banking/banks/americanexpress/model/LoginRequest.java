package com.liato.bankdroid.banking.banks.americanexpress.model;

import org.joda.time.DateTime;

public class LoginRequest {

    private String user;
    private String password;
    private long userTimeStampInMilli;

    public LoginRequest(String username, String password) {
        this.user = username;
        this.password = password;
        userTimeStampInMilli = DateTime.now().getMillis();
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public long getUserTimeStampInMilli() {
        return userTimeStampInMilli;
    }
}
