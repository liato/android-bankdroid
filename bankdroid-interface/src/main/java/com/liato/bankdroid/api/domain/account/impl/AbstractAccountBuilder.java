package com.liato.bankdroid.api.domain.account.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractAccountBuilder<T extends AbstractAccountBuilder<T>> {

    protected String mId;
    protected String mName;
    protected BigDecimal mBalance;
    protected String mCurrency;
    protected Map<String, String> mCustomAttributes;

    protected AbstractAccountBuilder(String id, String name, String currency) {
        if (id == null || id.isEmpty() || currency == null || currency.isEmpty() || name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Id, name and currency cannot be null or empty");
        }
        mId = id;
        mName = name;
        mCurrency = currency;
    }

    protected abstract T self();

    public T name(String name) {
        mName = name;
        return self();
    }

    public T balance(BigDecimal balance) {
        mBalance = balance;
        return self();
    }

    public T addCustomAttribute(String key, String value) {
        if (mCustomAttributes == null) {
            mCustomAttributes = new HashMap<>();
        }
        mCustomAttributes.put(key, value);
        return self();
    }

    public T customAttributes(Map<String, String> customAttributes) {
        mCustomAttributes = customAttributes;
        return self();
    }
}
