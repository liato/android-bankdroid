package com.liato.bankdroid.banking.banks.swedbank.model.engagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liato.bankdroid.banking.banks.swedbank.model.Account;
import com.liato.bankdroid.banking.banks.swedbank.model.CreditCard;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OverviewResponse {

    @JsonProperty
    private CreditCard creditCard;

    @JsonProperty
    private List<Account> transactionAccounts;

    @JsonProperty
    private List<Account> savingAccounts;

    @JsonProperty("loanAccounts")
    private List<Account> loanAccounts;

    @JsonProperty
    private List<Account> transactionDisposalAccounts;

    @JsonProperty
    private List<Account> savingDisposalAccounts;

    @JsonProperty
    private List<Account> cardAccounts;

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public List<Account> getTransactionAccounts() {
        if(transactionAccounts == null) {
            transactionAccounts = new ArrayList<Account>();
        }
        return transactionAccounts;
    }

    public void setTransactionAccounts(List<Account> transactionAccounts) {
        this.transactionAccounts = transactionAccounts;
    }

    public List<Account> getSavingAccounts() {
        if(savingAccounts == null) {
            savingAccounts = new ArrayList<Account>();
        }
        return savingAccounts;
    }

    public void setSavingAccounts(List<Account> savingAccounts) {
        this.savingAccounts = savingAccounts;
    }

    public List<Account> getLoanAccounts() {
        if(loanAccounts == null) {
            loanAccounts = new ArrayList<Account>();
        }
        return loanAccounts;
    }

    public void setLoanAccounts(List<Account> loanAccounts) {
        this.loanAccounts = loanAccounts;
    }

    public List<Account> getTransactionDisposalAccounts() {
        if(transactionDisposalAccounts == null) {
            transactionDisposalAccounts = new ArrayList<Account>();
        }
        return transactionDisposalAccounts;
    }

    public void setTransactionDisposalAccounts(List<Account> transactionDisposalAccounts) {
        this.transactionDisposalAccounts = transactionDisposalAccounts;
    }

    public List<Account> getSavingDisposalAccounts() {
        if(savingDisposalAccounts == null) {
            savingDisposalAccounts = new ArrayList<Account>();
        }
        return savingDisposalAccounts;
    }

    public void setSavingDisposalAccounts(List<Account> savingDisposalAccounts) {
        this.savingDisposalAccounts = savingDisposalAccounts;
    }

    public List<Account> getCardAccounts() {
        if(cardAccounts == null) {
            cardAccounts = new ArrayList<Account>();
        }
        return cardAccounts;
    }

    public void setCardAccounts(List<Account> cardAccounts) {
        this.cardAccounts = cardAccounts;
    }
}
