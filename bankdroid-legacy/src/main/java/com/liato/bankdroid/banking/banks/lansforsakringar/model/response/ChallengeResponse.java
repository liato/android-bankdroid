package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChallengeResponse {

    private int mLifetime;

    private String mToken;

    @JsonProperty("lifetime")
    public int getLifetime() {
        return mLifetime;
    }

    @JsonSetter("lifetime")
    public void setLifetime(int l) {
        mLifetime = l;
    }

    @JsonProperty("token")
    public String getToken() {
        return mToken;
    }

    @JsonSetter("token")
    public void setToken(String t) {
        mToken = t;
    }

}