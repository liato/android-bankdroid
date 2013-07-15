package com.liato.bankdroid.banking.banks.nordea.api.model.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.liato.bankdroid.util.ObjectUtils;

@JacksonXmlRootElement(localName = "lightLoginRequest")
public class LightLoginRequest {
    private String mType;
    private String mPassword;
    private String mUserId;

    public LightLoginRequest() {
    }

    public LightLoginRequest(String pUserId, String pPassword) {
        mUserId = pUserId;
        mPassword = pPassword;
        mType = "lightLoginSE";
    }

    public void setType(String pType) {
        mType = pType;
    }

    @JacksonXmlProperty(localName = "type")
    public String getType() {
        return mType;
    }

    public void setPassword(String pPassword) {
        mPassword = pPassword;
    }

    @JacksonXmlProperty(localName = "password")
    public String getPassword() {
        return mPassword;
    }

    public void setUserId(String pUserId) {
        mUserId = pUserId;
    }

    @JacksonXmlProperty(localName = "userId")
    public String getUserId() {
        return mUserId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LightLoginRequest other = (LightLoginRequest) obj;
        return ObjectUtils.equal(this.mPassword, other.mPassword) && ObjectUtils.equal(this.mType, other.mType)
                && ObjectUtils.equal(this.mUserId, other.mUserId);
    }
    
    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(this.mPassword,this.mType,this.mUserId);
    }
    
}