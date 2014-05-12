package com.liato.bankdroid.banking.banks.payson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @JsonProperty("Uri")
    private String uri;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("AmountDisplayValue")
    private String amountDisplayValue;
    @JsonProperty("CreditedAmountDisplayValue")
    private String creditedAmountDisplayValue;
    @JsonProperty("PurchaseId")
    private String purchaseId;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("Summary")
    private String summary;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("StatusId")
    private String statusId;
    @JsonProperty("PurchaseType")
    private String purchaseType;
    @JsonProperty("PurchaseTypeRaw")
    private String purchaseTypeRaw;
    @JsonProperty("IsActionable")
    private String isActionable;
    @JsonProperty("IsGuarantee")
    private String isGuarantee;
    @JsonProperty("ReceiverEmail")
    private String receiverEmail;
    @JsonProperty("SenderEmail")
    private String senderEmail;
    @JsonProperty("CurrencySymbol")
    private String currencySymbol;
    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("Uri")
    public String getUri() {
        return uri;
    }

    @JsonProperty("Uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("CreatedDate")
    public String getCreatedDate() {
        return createdDate;
    }

    @JsonProperty("CreatedDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @JsonProperty("AmountDisplayValue")
    public String getAmountDisplayValue() {
        return amountDisplayValue;
    }

    @JsonProperty("AmountDisplayValue")
    public void setAmountDisplayValue(String amountDisplayValue) {
        this.amountDisplayValue = amountDisplayValue;
    }

    @JsonProperty("CreditedAmountDisplayValue")
    public String getCreditedAmountDisplayValue() {
        return creditedAmountDisplayValue;
    }

    @JsonProperty("CreditedAmountDisplayValue")
    public void setCreditedAmountDisplayValue(String creditedAmountDisplayValue) {
        this.creditedAmountDisplayValue = creditedAmountDisplayValue;
    }

    @JsonProperty("PurchaseId")
    public String getPurchaseId() {
        return purchaseId;
    }

    @JsonProperty("PurchaseId")
    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    @JsonProperty("Message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("Message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("Summary")
    public String getSummary() {
        return summary;
    }

    @JsonProperty("Summary")
    public void setSummary(String summary) {
        this.summary = summary;
    }

    @JsonProperty("Status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("Status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("StatusId")
    public String getStatusId() {
        return statusId;
    }

    @JsonProperty("StatusId")
    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    @JsonProperty("PurchaseType")
    public String getPurchaseType() {
        return purchaseType;
    }

    @JsonProperty("PurchaseType")
    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    @JsonProperty("PurchaseTypeRaw")
    public String getPurchaseTypeRaw() {
        return purchaseTypeRaw;
    }

    @JsonProperty("PurchaseTypeRaw")
    public void setPurchaseTypeRaw(String purchaseTypeRaw) {
        this.purchaseTypeRaw = purchaseTypeRaw;
    }

    @JsonProperty("IsActionable")
    public String getIsActionable() {
        return isActionable;
    }

    @JsonProperty("IsActionable")
    public void setIsActionable(String isActionable) {
        this.isActionable = isActionable;
    }

    @JsonProperty("IsGuarantee")
    public String getIsGuarantee() {
        return isGuarantee;
    }

    @JsonProperty("IsGuarantee")
    public void setIsGuarantee(String isGuarantee) {
        this.isGuarantee = isGuarantee;
    }

    @JsonProperty("ReceiverEmail")
    public String getReceiverEmail() {
        return receiverEmail;
    }

    @JsonProperty("ReceiverEmail")
    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    @JsonProperty("SenderEmail")
    public String getSenderEmail() {
        return senderEmail;
    }

    @JsonProperty("SenderEmail")
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @JsonProperty("CurrencySymbol")
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    @JsonProperty("CurrencySymbol")
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    @JsonProperty("Amount")
    public String getAmount() {
        return amount;
    }

    @JsonProperty("Amount")
    public void setAmount(String amount) {
        this.amount = amount;
    }

}