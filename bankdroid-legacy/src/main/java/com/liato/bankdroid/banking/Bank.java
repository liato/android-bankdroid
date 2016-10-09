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

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.api.configuration.Field;
import com.liato.bankdroid.api.configuration.ProviderConfiguration;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.nullbyte.android.urllib.Urllib;

public abstract class Bank implements Comparable<Bank>, IBankTypes {

    private final ProviderConfiguration defaultConfiguration = new LegacyProviderConfiguration(this);

    @DrawableRes
    private final int logoResource;

    protected String TAG = "Bank";

    protected String NAME = "Bank";

    protected String NAME_SHORT = "bank";

    protected int BANKTYPE_ID = 0;

    /**
     * URL for human-accessible web bank.
     * <p/>
     * Can be set to null to disable. Lots of banks don't have this any more, but have
     * apps instead.
     * <p/>
     * @see #isWebViewEnabled()
     */
    @Nullable
    protected String URL;

    protected int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;

    protected int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_VARIATION_PASSWORD;

    protected int INPUT_TYPE_EXTRAS = InputType.TYPE_CLASS_TEXT;

    protected String INPUT_HINT_USERNAME = null;

    protected boolean INPUT_HIDDEN_USERNAME = false;

    protected boolean INPUT_HIDDEN_PASSWORD = false;

    protected boolean INPUT_HIDDEN_EXTRAS = true;

    protected int INPUT_TITLETEXT_USERNAME = R.string.username;

    protected int INPUT_TITLETEXT_PASSWORD = R.string.password;

    protected int INPUT_TITLETEXT_EXTRAS = R.string.extras_field;

    protected boolean STATIC_BALANCE = false;

    protected boolean BROKEN = false;

    protected boolean DISPLAY_DECIMALS = true;

    /**
     * Whether or not we support opening the web version of a bank.
     * <p/>
     * Lots of banks don't have this any more, but have apps instead.
     * @see #isWebViewEnabled()
     * @see #URL
     */
    protected boolean WEB_VIEW_ENABLED = true;

    protected Context context;

    protected Resources res;

    protected String customName;

    protected String currency = "SEK";

    protected ArrayList<Account> accounts = new ArrayList<Account>();

    protected HashMap<String, Account> oldAccounts;

    protected BigDecimal balance = new BigDecimal(0);

    protected boolean disabled = false;

    protected long dbid = -1;

    protected Urllib urlopen = null;

    protected boolean hideAccounts = false;

    private Map<String, String> properties;

    public Bank(Context context, @DrawableRes int logoResource) {
        this.context = context;
        this.res = this.context.getResources();
        this.logoResource = logoResource;
    }

    public boolean toggleHideAccounts() {
        hideAccounts = !hideAccounts;
        return hideAccounts;
    }

    public boolean getHideAccounts() {
        return hideAccounts;
    }

    public Urllib getUrlopen() {
        return urlopen;
    }

    public void setUrlopen(Urllib urlopen) {
        this.urlopen = urlopen;
    }

    public void setDbid(long dbid) {
        this.dbid = dbid;
    }

    public void update(String username, String password) throws BankException, LoginException,
            BankChoiceException, IOException {
        setUsername(username);
        setPassword(password);
        this.update();
    }

    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        balance = new BigDecimal(0);
        oldAccounts = new HashMap<String, Account>();
        for (Account account : accounts) {
            oldAccounts.put(account.getId(), account);
        }
        accounts = new ArrayList<Account>();
    }

    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
    }

    public void updateAllTransactions() throws LoginException, BankException, IOException {
        if (urlopen == null) {
            urlopen = login();
        }
        for (Account account : accounts) {
            updateTransactions(account, urlopen);
        }
    }

    public Urllib login() throws LoginException, BankException, IOException {
        return null;
    }

    public void closeConnection() {
        if (urlopen != null) {
            urlopen.close();
        }
    }

    public ArrayList<Account> getAccounts() {
        return this.accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
        for (Account a : accounts) {
            a.setBank(this);
        }
    }

    public String getPassword() {
        String password = getProperty(LegacyProviderConfiguration.PASSWORD);
        return password == null ? "" : password;
    }

    public void setPassword(String password) {
        getProperties().put(LegacyProviderConfiguration.PASSWORD, password);
    }

    public String getUsername() {
        String username = getProperty(LegacyProviderConfiguration.USERNAME);
        return username == null ? "" : username;
    }

    public void setUsername(String username) {
        getProperties().put(LegacyProviderConfiguration.USERNAME, username);
    }

    public BigDecimal getBalance() {
        if (STATIC_BALANCE) {
            return balance;
        } else {
            BigDecimal bal = new BigDecimal(0);
            for (Account account : accounts) {
                if (account.getType() == Account.REGULAR || account.getType() == Account.CCARD) {
                    if (!account.isHidden() && (account.getAliasfor() == null
                            || account.getAliasfor().length() == 0)
                            && account.getBalance() != null) {
                        bal = bal.add(account.getBalance());
                    }
                }
            }
            return bal;
        }
    }

    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    public String getName() {
        return NAME;
    }

    public String getDisplayName() {
        if (customName != null && customName.length() > 0) {
            return customName;
        }
        return getUsername();
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getExtras() {
        String extras = getProperty(LegacyProviderConfiguration.EXTRAS);
        return extras == null ? "" : extras;
    }

    public void setExtras(String extras) {
        getProperties().put(LegacyProviderConfiguration.EXTRAS, extras);
      }

    public String getShortName() {
        return NAME_SHORT;
    }

    public void setData(BigDecimal balance,
            boolean disabled, long dbid, String currency, String customName,
            int hideAccounts) {
        this.balance = balance;
        this.disabled = disabled;
        this.dbid = dbid;
        this.currency = currency;
        this.customName = customName;
        this.hideAccounts = hideAccounts == 1 ? true : false;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getDbId() {
        return dbid;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getURL() {
        return URL;
    }

    public int getInputTypeUsername() {
        return INPUT_TYPE_USERNAME;
    }

    public int getInputTypePassword() {
        return INPUT_TYPE_PASSWORD;
    }

    public int getInputTypeExtras() {
        return INPUT_TYPE_EXTRAS;
    }

    public String getInputHintUsername() {
        return INPUT_HINT_USERNAME;
    }

    public boolean isInputUsernameHidden() {
        return INPUT_HIDDEN_USERNAME;
    }

    public boolean isInputPasswordHidden() {
        return INPUT_HIDDEN_PASSWORD;
    }

    public boolean isInputExtrasHidden() {
        return INPUT_HIDDEN_EXTRAS;
    }

    public int getInputTitleUsername() {
        return INPUT_TITLETEXT_USERNAME;
    }

    public int getInputTitlePassword() {
        return INPUT_TITLETEXT_PASSWORD;
    }

    public int getInputTitleExtras() {
        return INPUT_TITLETEXT_EXTRAS;
    }

    /**
     * Whether or not we support opening the web version of a bank.
     * <p/>
     * Lots of banks don't have this any more, but have apps instead.
     * @see #WEB_VIEW_ENABLED
     * @see #URL
     */
    public boolean isWebViewEnabled() {
        return URL != null && WEB_VIEW_ENABLED;
    }

    public Map<String, String> getProperties() {
        if(this.properties == null) {
            this.properties = new HashMap<>();
        }
        return this.properties;
    }

    public String getProperty(String name) {
        return getProperties().get(name);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    // Returns true if the current implementation of this bank is broken.
    public boolean isBroken() {
        return BROKEN;
    }

    @DrawableRes
    public final int getImageResource() {
        return logoResource;
    }

    public int compareTo(Bank another) {
        return this.getName().compareToIgnoreCase(another.getName());
    }

    public List<Field> getConnectionConfiguration() {
        return defaultConfiguration.getConnectionConfiguration();
    }

    public void updateComplete() {
        for (Account a : this.accounts) {
            //Preserve hidden and notify settings from old accounts
            if (oldAccounts != null) {
                Account oa = oldAccounts.get(a.getId());
                if (oa != null) {
                    a.setHidden(oa.isHidden());
                    a.setNotify(oa.isNotify());
                    a.setCurrency(oa.getCurrency());
                }
            }
            a.setBank(this);
        }
    }

    public SessionPackage getSessionPackage(Context context) {
        String preloader = "Error...";
        try {
            preloader = IOUtils.toString(context.getResources().openRawResource(R.raw.loading));
        } catch (NotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            LoginPackage lp = preLogin();
            if (lp == null) {
                throw new BankException(
                        "No automatic login for this bank. preLogin() is not implemented or has failed.");
            }
            //TODO: Skip the form submission. Login using Bank.login(...) and transfer cookies to webview. The user is now logged in
            //      and can me directed to any page.
            String html = "";
            if (!lp.isLoggedIn()) {
                html = String.format(preloader,
                        "function go(){document.getElementById('submitform').submit(); }",
                        // Javascript function
                        Helpers.renderForm(lp.getLoginTarget(), lp.getPostData())
                                + "<script type=\"text/javascript\">setTimeout('go()', 1000);</script>"
                        // HTML
                );
            } else {
                html = String.format(preloader,
                        String.format("function go(){window.location=\"%s\" }",
                                lp.getLoginTarget()), // Javascript function
                        "<script type=\"text/javascript\">setTimeout('go()', 100);</script>" // HTML
                );
            }

            CookieStore cookies = urlopen.getHttpclient().getCookieStore();
            return new SessionPackage(html, cookies);
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (BankException e) {
            Log.e(TAG, e.getMessage());
        }
        String html = String.format(preloader,
                String.format("function go(){window.location=\"%s\" }", this.URL),
                // Javascript function
                "<script type=\"text/javascript\">setTimeout('go()', 1000);</script>" // HTML
        );
        return new SessionPackage(html, null);
    }

    protected LoginPackage preLogin() throws BankException, IOException {
        return null;
    }

    public boolean getDisplayDecimals() {
        return DISPLAY_DECIMALS;
    }

    protected Context getContext() {
        return context;
    }

    public DecimalFormat getDecimalFormatter() {
        return null;
    }

    public static class SessionPackage {

        private String html;

        private CookieStore cookiestore;

        public SessionPackage(String html, CookieStore cookiestore) {
            this.html = html;
            this.cookiestore = cookiestore;
        }

        public String getHtml() {
            return html;
        }

        public CookieStore getCookiestore() {
            return cookiestore;
        }
    }

    public static class LoginPackage {

        private String response;

        private Urllib urllib;

        private List<NameValuePair> postData;

        private String loginTarget;

        private boolean isLoggedIn = false;

        public LoginPackage(Urllib urllib, List<NameValuePair> postData, String response,
                String loginTarget) {
            this.urllib = urllib;
            this.postData = postData;
            this.response = response;
            this.loginTarget = loginTarget;
        }

        public void setIsLoggedIn(boolean loggedIn) {
            this.isLoggedIn = loggedIn;
        }

        public String getResponse() {
            return response;
        }

        public Urllib getUrllib() {
            return urllib;
        }

        public List<NameValuePair> getPostData() {
            return postData;
        }

        public String getLoginTarget() {
            return loginTarget;
        }

        public boolean isLoggedIn() {
            return this.isLoggedIn;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Bank bank = (Bank) obj;
        return bank.getBanktypeId() == this.getBanktypeId();
    }

    @Override
    public int hashCode() {
        return getBanktypeId();
    }
}
