package com.liato.bankdroid.banking.banks.americanexpress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogonData {

    private int status;
    private String message;
    private String cupcake;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCupcake() {
        return cupcake;
    }

    public void setCupcake(String cupcake) {
        this.cupcake = cupcake;
    }
}
