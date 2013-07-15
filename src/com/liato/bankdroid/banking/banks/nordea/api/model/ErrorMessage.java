package com.liato.bankdroid.banking.banks.nordea.api.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.liato.bankdroid.util.ObjectUtils;

public class ErrorMessage {

    private String mErrorCode;

    public void setErrorCode(String pErrorCode) {
        mErrorCode = pErrorCode;
    }

    @JacksonXmlProperty(localName = "errorCode")
    public String getErrorCode() {
        return mErrorCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ErrorMessage other = (ErrorMessage) obj;
        return ObjectUtils.equal(this.mErrorCode, other.mErrorCode);
    }
    
    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(mErrorCode);
    }
}
