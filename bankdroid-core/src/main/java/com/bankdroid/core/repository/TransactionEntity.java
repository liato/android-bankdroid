package com.bankdroid.core.repository;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class TransactionEntity {

    private final String id;
    private final String description;
    private final BigDecimal amount;
    private final String currency;
    private final DateTime dateTime;
    private final boolean pending;

    private TransactionEntity(
            String id,
            String description,
            BigDecimal amount,
            String currency,
            DateTime dateTime,
            boolean pending) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.dateTime = dateTime;
        this.pending = pending;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public DateTime date() {
        return dateTime;
    }

    public boolean pending() {
        return pending;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private String description;
        private BigDecimal amount;
        private String currency;
        private DateTime dateTime;
        private boolean pending;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder date(DateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder pending(boolean pending) {
            this.pending = pending;
            return this;
        }

        public TransactionEntity build() {
            return new TransactionEntity(
                    id,
                    description,
                    amount,
                    currency,
                    dateTime,
                    pending
            );
        }
    }
}
