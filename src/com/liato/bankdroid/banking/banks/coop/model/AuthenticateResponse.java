package com.liato.bankdroid.banking.banks.coop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateResponse extends BaseResponse {

    @JsonProperty("AuthenticateResult")
    private AuthenticateResult authenticateResult;

    @JsonProperty("AuthenticateResult")
    public AuthenticateResult getAuthenticateResult() {
        return authenticateResult;
    }

    @JsonProperty("AuthenticateResult")
    public void setAuthenticateResult(AuthenticateResult authenticateResult) {
        this.authenticateResult = authenticateResult;
    }

}