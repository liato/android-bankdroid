/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liato.bankdroid.banking;

import java.math.BigDecimal;

public class Transaction implements Comparable<Transaction> {

    private String date;

    private String transaction;

    private BigDecimal amount;

    private String currency;

    public Transaction(String date, String transaction, BigDecimal amount, String currency) {
        this.date = date;
        this.transaction = transaction;
        this.amount = amount;
        this.currency = currency;
    }

    public Transaction(String date, String transaction, BigDecimal amount) {
        this(date, transaction, amount, "SEK");
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public int compareTo(Transaction another) {
        try {
            Integer thisdate = Integer.parseInt(date.replaceAll("-", ""));
            Integer thatdate = Integer.parseInt((another).getDate().replaceAll("-", ""));
            return thatdate - thisdate;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
