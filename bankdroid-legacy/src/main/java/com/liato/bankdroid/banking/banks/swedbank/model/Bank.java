package com.liato.bankdroid.banking.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bank {

    @JsonProperty
    private String name;

    @JsonProperty
    private String bankId;

    @JsonProperty
    private Profile privateProfile;

    @JsonProperty
    private List<CorporateProfile> corporateProfiles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public Profile getPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(Profile privateProfile) {
        this.privateProfile = privateProfile;
    }

    public List<CorporateProfile> getCorporateProfiles() {
       if(corporateProfiles == null) {
           corporateProfiles = new ArrayList<CorporateProfile>();
       }
        return corporateProfiles;
    }

    public void setCorporateProfiles(List<CorporateProfile> corporateProfiles) {
        this.corporateProfiles = corporateProfiles;
    }
}


