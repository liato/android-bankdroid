package com.liato.bankdroid.banking.banks.icabanken.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liato.bankdroid.banking.banks.icabanken.model.IcaBankenAccountList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    private String mABCustomerId;

    private IcaBankenAccountList mAccountList;

    private String mSessionId;

    @JsonProperty("ABCustomerId")
    public String getABCustomerId() {
        return mABCustomerId;
    }

    public void setABCustomerId(String pABCustomerId) {
        this.mABCustomerId = pABCustomerId;
    }

    @JsonProperty("AccountList")
    public IcaBankenAccountList getAccountList() {
        return mAccountList;
    }

    public void setAccountList(IcaBankenAccountList pAccountList) {
        this.mAccountList = pAccountList;
    }

    @JsonProperty("SessionId")
    public String getSessionId() {
        return mSessionId;
    }

    public void setSessionId(String pSessionId) {
        this.mSessionId = pSessionId;
    }
}
