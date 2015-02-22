package com.liato.bankdroid.banking.banks.swedbank.model.identification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liato.bankdroid.banking.banks.swedbank.model.OperationalMessages;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalCodeResponse {

    @JsonProperty
    private boolean personalCodeChangeRequired;

    @JsonProperty
    private List<OperationalMessages> operationalMessages;

    public boolean getPersonalCodeChangeRequired() {
        return personalCodeChangeRequired;
    }

    public void setPersonalCodeChangeRequired(boolean personalCodeChangeRequired) {
        this.personalCodeChangeRequired = personalCodeChangeRequired;
    }

    public List<OperationalMessages> getOperationalMessages() {
        if(operationalMessages == null) {
            operationalMessages = new ArrayList<OperationalMessages>();
        }
        return operationalMessages;
    }

    public void setOperationalMessages(List<OperationalMessages> operationalMessages) {
        this.operationalMessages = operationalMessages;
    }
}
