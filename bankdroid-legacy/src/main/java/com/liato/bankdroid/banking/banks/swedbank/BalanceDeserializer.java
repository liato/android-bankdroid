package com.liato.bankdroid.banking.banks.swedbank;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.liato.bankdroid.Helpers;

import java.io.IOException;
import java.math.BigDecimal;


public class BalanceDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        return Helpers.parseBalance(jp.getValueAsString());

    }
}
