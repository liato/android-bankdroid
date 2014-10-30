package com.liato.bankdroid.banking.banks.swedbank.model.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liato.bankdroid.banking.banks.swedbank.model.Bank;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResponse {

    @JsonProperty("hasSwedbankProfile")
    private boolean swedbankProfile;

    @JsonProperty("hasSavingbankProfile")
    private boolean savingbankProfile;

    @JsonProperty
    private List<Bank> banks;

    public boolean isSwedbankProfile() {
        return swedbankProfile;
    }

    public void setSwedbankProfile(boolean swedbankProfile) {
        this.swedbankProfile = swedbankProfile;
    }

    public boolean isSavingbankProfile() {
        return savingbankProfile;
    }

    public void setSavingbankProfile(boolean savingbankProfile) {
        this.savingbankProfile = savingbankProfile;
    }

    public List<Bank> getBanks() {
        if(banks == null) {
            banks = new ArrayList<Bank>();
        }
        return banks;
    }

    public void setBanks(List<Bank> banks) {
        this.banks = banks;
    }
}
