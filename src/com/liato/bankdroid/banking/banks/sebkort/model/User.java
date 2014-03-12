package com.liato.bankdroid.banking.banks.sebkort.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = -6588506336066035322L;

    private String mSurName;
    private String mFirstName;
    private boolean mPasswordAuthentication;
    private boolean mCoBrowse;
    private int mAuthenticationTypeId;
    private String mPhone;
    private boolean mHsu;
    private String mEmail;
    private boolean mOtpAuthentication;


    @JsonSetter("surName")
    public void setSurName(String s) {
        mSurName = s;
    }

    @JsonProperty("surName")
    public String getSurName() {
        return mSurName;
    }

    @JsonSetter("firstName")
    public void setFirstName(String f) {
        mFirstName = f;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return mFirstName;
    }

    @JsonSetter("passwordAuthentication")
    public void setPasswordAuthentication(boolean p) {
        mPasswordAuthentication = p;
    }

    @JsonProperty("passwordAuthentication")
    public boolean getPasswordAuthentication() {
        return mPasswordAuthentication;
    }

    @JsonSetter("coBrowse")
    public void setCoBrowse(boolean c) {
        mCoBrowse = c;
    }

    @JsonProperty("coBrowse")
    public boolean getCoBrowse() {
        return mCoBrowse;
    }

    @JsonSetter("authenticationTypeId")
    public void setAuthenticationTypeId(int a) {
        mAuthenticationTypeId = a;
    }

    @JsonProperty("authenticationTypeId")
    public int getAuthenticationTypeId() {
        return mAuthenticationTypeId;
    }

    @JsonSetter("phone")
    public void setPhone(String p) {
        mPhone = p;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return mPhone;
    }

    @JsonSetter("hsu")
    public void setHsu(boolean h) {
        mHsu = h;
    }

    @JsonProperty("hsu")
    public boolean getHsu() {
        return mHsu;
    }

    @JsonSetter("email")
    public void setEmail(String e) {
        mEmail = e;
    }

    @JsonProperty("email")
    public String getEmail() {
        return mEmail;
    }

    @JsonSetter("otpAuthentication")
    public void setOtpAuthentication(boolean o) {
        mOtpAuthentication = o;
    }

    @JsonProperty("otpAuthentication")
    public boolean getOtpAuthentication() {
        return mOtpAuthentication;
    }

}