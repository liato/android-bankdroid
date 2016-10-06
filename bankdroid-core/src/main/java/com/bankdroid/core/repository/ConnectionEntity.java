package com.bankdroid.core.repository;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionEntity {

    public static final long DEFAULT_ID  = -1;

    private long id;

    private String providerId;

    private String name;

    private boolean enabled;

    private DateTime lastUpdated;

    private Map<String,String> properties;

    private Collection<AccountEntity> accounts;

    private ConnectionEntity(
            long id,
            String providerId,
            String name,
            boolean enabled,
            DateTime lastUpdated,
            Map<String,String> properties,
            Collection<AccountEntity> accounts
    ) {
        this.id = id;
        this.providerId = providerId;
        this.name = name;
        this.enabled = enabled;
        this.lastUpdated = lastUpdated;
        this.properties = properties;
        this.accounts = accounts;
    }

    public long id() {
        return id;
    }

    public String providerId() {
        return providerId;
    }

    public String name() {
        return name;
    }

    public boolean enabled() {
        return enabled;
    }

    public DateTime lastUpdated() {
        return lastUpdated;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public Collection<AccountEntity> accounts()           {
        return accounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionEntity that = (ConnectionEntity) o;

        if (id != that.id) return false;
        if (enabled != that.enabled) return false;
        if (providerId != null ? !providerId.equals(that.providerId) : that.providerId != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (lastUpdated != null ? !lastUpdated.equals(that.lastUpdated) : that.lastUpdated != null)
            return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null)
            return false;
        return accounts != null ? accounts.equals(that.accounts) : that.accounts == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (accounts != null ? accounts.hashCode() : 0);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long id;

        private String providerId;

        private String name;

        private boolean enabled;

        private DateTime lastUpdated;

        private Map<String,String> properties = new HashMap<>();

        private Collection<AccountEntity> accounts = new ArrayList<>();

        private Builder() {}

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder lastUpdated(DateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder properties(Map<String,String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public Builder property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public Builder account(AccountEntity account) {
            accounts.add(account);
            return this;
        }

        public Builder accounts(Collection<AccountEntity> accounts) {
            this.accounts.addAll(accounts);
            return this;
        }

        public ConnectionEntity build() {
            return new ConnectionEntity(
                    id,
                    providerId,
                    name,
                    enabled,
                    lastUpdated,
                    properties,
                    accounts
            );
        }
    }
}
