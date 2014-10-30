package com.liato.bankdroid.banking.banks.swedbank.model.identification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalCodeRequest {

    /**
     * May be a personal number or a fictive personal number. Format YYYYMMDD-NNNN.
     */
    @JsonProperty
    private String userId;

    @JsonProperty
    private String password;

    public PersonalCodeRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
