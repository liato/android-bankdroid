package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class LoginResponse {

    private String mName;

    private String mLfCompanyBelonging;

    private String mSsn;

    private int mTicketLifetime;

    private boolean mPinPadAvailable;

    private String mTicket;

    @JsonProperty("name")
    public String getName() {
        return mName;
    }

    @JsonSetter("name")
    public void setName(String n) {
        mName = n;
    }

    @JsonProperty("lfCompanyBelonging")
    public String getLfCompanyBelonging() {
        return mLfCompanyBelonging;
    }

    @JsonSetter("lfCompanyBelonging")
    public void setLfCompanyBelonging(String l) {
        mLfCompanyBelonging = l;
    }

    @JsonProperty("ssn")
    public String getSsn() {
        return mSsn;
    }

    @JsonSetter("ssn")
    public void setSsn(String s) {
        mSsn = s;
    }

    @JsonProperty("ticketLifetime")
    public int getTicketLifetime() {
        return mTicketLifetime;
    }

    @JsonSetter("ticketLifetime")
    public void setTicketLifetime(int t) {
        mTicketLifetime = t;
    }

    @JsonProperty("pinPadAvailable")
    public boolean getPinPadAvailable() {
        return mPinPadAvailable;
    }

    @JsonSetter("pinPadAvailable")
    public void setPinPadAvailable(boolean p) {
        mPinPadAvailable = p;
    }

    @JsonProperty("ticket")
    public String getTicket() {
        return mTicket;
    }

    @JsonSetter("ticket")
    public void setTicket(String t) {
        mTicket = t;
    }

}