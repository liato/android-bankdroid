package com.liato.bankdroid.banking.banks.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Input implements Serializable {

    private static final long serialVersionUID = 7507419745749485877L;

    @JsonProperty("prev_out")
    private PrevOut mPrevOut;

    @JsonProperty("prev_out")
    public PrevOut getPrevOut() {
        return mPrevOut;
    }

}