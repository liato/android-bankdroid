package com.liato.bankdroid.banking.banks.sebkort.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

public abstract class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 2771913870986291964L;

    private String mErrorCode;

    private String mMessage;

    private String mReturnCode;

    private T mBody;

    @JsonProperty("errorCode")
    public String getErrorCode() {
        return mErrorCode;
    }

    @JsonSetter("errorCode")
    public void setErrorCode(String e) {
        mErrorCode = e;
    }

    @JsonProperty("body")
    public T getBody() {
        return mBody;
    }

    @JsonSetter("body")
    public void setBody(T b) {
        mBody = b;
    }

    @JsonProperty("message")
    public String getMessage() {
        return mMessage;
    }

    @JsonSetter("message")
    public void setMessage(String m) {
        mMessage = m;
    }

    @JsonProperty("returnCode")
    public String getReturnCode() {
        return mReturnCode;
    }

    @JsonSetter("returnCode")
    public void setReturnCode(String r) {
        mReturnCode = r;
    }

}