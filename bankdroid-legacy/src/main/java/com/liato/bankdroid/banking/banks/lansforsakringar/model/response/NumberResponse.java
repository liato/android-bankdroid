package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class NumberResponse {

    private int mNumber;

    private String mNumberPair;

    @JsonProperty("number")
    public int getNumber() {
        return mNumber;
    }

    @JsonSetter("number")
    public void setNumber(int n) {
        mNumber = n;
    }

    @JsonProperty("numberPair")
    public String getNumberPair() {
        return mNumberPair;
    }

    @JsonSetter("numberPair")
    public void setNumberPair(String n) {
        mNumberPair = n;
    }

}