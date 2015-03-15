package com.liato.bankdroid.banking.banks.ica.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginError {

    @JsonProperty("Title")
    private String title;

    @JsonProperty("MessageCode")
    private String messageCode;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("Title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("MessageCode")
    public String getMessageCode() {
        return messageCode;
    }

    @JsonProperty("MessageCode")
    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    @JsonProperty("PhoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonProperty("PhoneNumber")
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @JsonProperty("Message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("Message")
    public void setMessage(String message) {
        this.message = message;
    }

}