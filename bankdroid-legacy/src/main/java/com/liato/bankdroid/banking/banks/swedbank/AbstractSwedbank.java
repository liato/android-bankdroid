package com.liato.bankdroid.banking.banks.swedbank;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankChoice;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.swedbank.model.CardAccount;
import com.liato.bankdroid.banking.banks.swedbank.model.CardTransaction;
import com.liato.bankdroid.banking.banks.swedbank.model.ErrorMessage;
import com.liato.bankdroid.banking.banks.swedbank.model.ErrorResponse;
import com.liato.bankdroid.banking.banks.swedbank.model.engagement.CardAccountResponse;
import com.liato.bankdroid.banking.banks.swedbank.model.engagement.OverviewResponse;
import com.liato.bankdroid.banking.banks.swedbank.model.engagement.TransactionsResponse;
import com.liato.bankdroid.banking.banks.swedbank.model.identification.PersonalCodeRequest;
import com.liato.bankdroid.banking.banks.swedbank.model.profile.ProfileResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.utils.Installation;
import com.liato.bankdroid.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.HttpMethod;
import eu.nullbyte.android.urllib.Urllib;

public abstract class AbstractSwedbank extends Bank {

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";

    private static final boolean WEB_VIEW_ENABLED = false;

    private static final String API_BASE
            = "https://auth.api.swedbank.se/TDE_DAP_Portal_REST_WEB/api/v1/";

    private ObjectMapper mObjectMapper = new ObjectMapper();

    private Map<String, String> mIdMap = new HashMap<String, String>();

    public AbstractSwedbank(Context context) {
        super(context);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BigDecimal.class, new BalanceDeserializer());
        mObjectMapper.registerModule(module);

        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.WEB_VIEW_ENABLED = WEB_VIEW_ENABLED;
    }

    public AbstractSwedbank(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_swedbank));
        urlopen.addHeader("Authorization", getAuthenticationHeader());
        urlopen.addHeader("Content-Type", "application/json;charset=UTF-8");
        urlopen.addHeader("Accept", "application/json");
        return new LoginPackage(urlopen, null, null, getResourceUri("identification/personalcode"));
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        HttpResponse httpResponse = null;
        try {
            LoginPackage lp = preLogin();
            httpResponse = urlopen.openAsHttpResponse(lp.getLoginTarget(),
                    new StringEntity(objectAsJson(new PersonalCodeRequest(getUsername(), getPassword())),
                            HTTP.UTF_8), true);
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            if (responseCode == 201) {
                return urlopen;
            } else if (responseCode == 401 || responseCode == 400) {
                throw new LoginException(res.getText(
                        R.string.invalid_username_password).toString());
            } else if (responseCode == 503) {
                String errorMessage = null;
                try {
                    ErrorResponse er = readJsonValue(httpResponse.getEntity().getContent(),
                            ErrorResponse.class);
                    StringBuilder sb = new StringBuilder();
                    for (List<ErrorMessage> ems : er.getErrorMessages().values()) {
                        for (ErrorMessage em : ems) {
                            sb.append(Html.fromHtml(em.getMessage()).toString()).append("\n");
                        }
                    }
                    errorMessage = sb.toString().trim();
                } catch (BankException e) {
                    //Ignore json parse errors and show generic server error message
                }
                throw new BankException(TextUtils.isEmpty(errorMessage) ? context
                        .getString(R.string.server_error_try_again) : errorMessage);
            } else {
                throw new BankException("");
            }
        } finally {
            if (httpResponse != null) {
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity != null) {
                    try {
                        httpEntity.consumeContent();
                    } catch (IOException e) {
                        throw new BankException(e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();

        ProfileResponse profileResponse = getAvailableProfiles();
        setDefaultProfile(getBankId(profileResponse.getBanks()));

        HttpResponse httpResponse = urlopen
                .openAsHttpResponse(getResourceUri("engagement/overview"), false);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new BankException(httpResponse.getStatusLine().toString());
        }

        OverviewResponse overviewResponse = readJsonValue(httpResponse.getEntity().getContent(),
                OverviewResponse.class);
        addAccounts(overviewResponse.getTransactionAccounts(), Account.REGULAR);
        addAccounts(overviewResponse.getSavingAccounts(), Account.REGULAR);
        addAccounts(overviewResponse.getTransactionDisposalAccounts(), Account.REGULAR);
        addAccounts(overviewResponse.getSavingDisposalAccounts(), Account.REGULAR);
        addCardAccounts(overviewResponse.getCardAccounts());
        addAccounts(overviewResponse.getLoanAccounts(), Account.LOANS);
        if (this.accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);
        if (account.getType() == Account.CCARD) {
            updateCreditCardTransactions(account, urlopen);
            return;
        } else if (account.getType() != Account.REGULAR) {
            return;
        } else if (mIdMap.get(account.getId()) == null) {
            return;
        }

        HttpResponse httpResponse = urlopen.openAsHttpResponse(
                getResourceUri("engagement/transactions/" + mIdMap.get(account.getId())), false);

        TransactionsResponse response = readJsonValue(httpResponse.getEntity().getContent(),
                TransactionsResponse.class);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.addAll(transformTransactions(response.getTransactions()));
        transactions.addAll(transformTransactions(response.getReservedTransactions()));
        account.setTransactions(transactions);
    }

    @Override
    public void closeConnection() {
        try {
            HttpResponse response = urlopen
                    .openAsHttpResponse(getResourceUri("identification/logout"), HttpMethod.PUT);
        } catch (IOException e) {
            //Ignore logout exceptions
        } finally {
            super.closeConnection();
        }
    }

    private void updateCreditCardTransactions(Account account, Urllib urlopen) throws BankException,
            IOException {
        HttpResponse httpResponse = urlopen.openAsHttpResponse(
                getResourceUri("engagement/cardaccount/" + mIdMap.get(account.getId())), false);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            Log.i(TAG, "Couldn't find transactions for creditcard");
            account.setTransactions(Collections.<Transaction>emptyList());
            return;
        }
        CardAccountResponse response = readJsonValue(httpResponse.getEntity().getContent(),
                CardAccountResponse.class);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.addAll(transformCardTransactions(response.getTransactions()));
        transactions.addAll(transformCardTransactions(response.getReservedTransactions()));
        account.setTransactions(transactions);
    }

    private List<Transaction> transformTransactions(
            List<com.liato.bankdroid.banking.banks.swedbank.model.Transaction> transactions) {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        for (com.liato.bankdroid.banking.banks.swedbank.model.Transaction transaction : transactions) {
            transactionList.add(new Transaction(transaction.getDate(), transaction.getDescription(),
                    transaction.getAmount(), transaction.getCurrency()));
        }
        return transactionList;
    }

    private List<Transaction> transformCardTransactions(List<CardTransaction> transactions) {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        for (CardTransaction transaction : transactions) {
            transactionList.add(new Transaction(transaction.getDate(),
                    transaction.getDescription(),
                    transaction.getLocalAmount().getAmount(),
                    transaction.getLocalAmount().getCurrencyCode()));
        }
        return transactionList;
    }

    private ProfileResponse getAvailableProfiles()
            throws IOException, BankException, LoginException {
        HttpResponse httpResponse = urlopen.openAsHttpResponse(getResourceUri("profile/"), false);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            ProfileResponse response = readJsonValue(httpResponse.getEntity().getContent(),
                    ProfileResponse.class);
            if (response.getBanks().isEmpty()) {
                String provider = response.isSwedbankProfile() ? "Swedbank"
                        : response.isSavingbankProfile() ? "Sparbankerna" : null;
                if (provider != null) {
                    throw new LoginException(
                            "You are trying to connect an account from " + provider + " to the "
                                    + NAME + " bank. Please use the " + provider
                                    + " bank instead.");
                } else {
                    throw new BankException("No profiles available.");
                }
            }
            return response;
        }
        throw new BankException("Could not fetch available profiles.");
    }

    private void setDefaultProfile(String bankId) throws IOException, BankException {
        HttpResponse httpResponse = urlopen
                .openAsHttpResponse(getResourceUri("profile/private/" + bankId), true);
        httpResponse.getEntity().consumeContent();
        if (httpResponse.getStatusLine().getStatusCode() != 201) {
            throw new BankException("Could not set the default profile.");
        }
    }

    private String getBankId(List<com.liato.bankdroid.banking.banks.swedbank.model.Bank> bankList)
            throws BankChoiceException {
        String bankId = getExtras();
        if (bankId != null && !bankId.isEmpty()) {
            return bankId;
        }
        if (bankList.size() > 1) {
            ArrayList<BankChoice> banks = new ArrayList<BankChoice>();
            for (com.liato.bankdroid.banking.banks.swedbank.model.Bank bank : bankList) {
                banks.add(new BankChoice(bank.getName(), bank.getBankId()));
            }
            throw new BankChoiceException("Select a bank.", banks);
        }
        return bankList.get(0).getBankId();
    }

    private void addAccounts(
            List<com.liato.bankdroid.banking.banks.swedbank.model.Account> accountList,
            int accountType) {
        for (com.liato.bankdroid.banking.banks.swedbank.model.Account account : accountList) {
            Account bankdroidAccount = new Account(account.getName(), account.getBalance(),
                    account.getFullyFormattedNumber(), accountType, account.getCurrency());
            mIdMap.put(bankdroidAccount.getId(), account.getId());
            this.accounts.add(bankdroidAccount);
        }
    }

    private void addCardAccounts(List<CardAccount> accountList) {
        for (CardAccount account : accountList) {
            String currency = account.getCurrency() == null ? "SEK" : account.getCurrency();
            Account bankdroidAccount = new Account(account.getName(), account.getAvailableAmount(),
                    account.getCardNumber(), Account.CCARD, currency);
            mIdMap.put(bankdroidAccount.getId(), account.getId());
            this.accounts.add(bankdroidAccount);
        }
    }

    private String getAuthenticationHeader() {
        byte[] data = StringUtils.getBytes(getAppId() +
                ':' +
                Installation.id(context));
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    private String getResourceUri(String resource) {
        String dsid = "dsid=" + UUID.randomUUID().toString();
        urlopen.addHeader("Cookie", dsid);
        return API_BASE + resource + '?' + dsid;
    }

    private <T> T readJsonValue(InputStream is, Class<T> valueType) throws BankException, IOException {
        try {
            return mObjectMapper.readValue(is, valueType);
        } catch(JsonParseException | JsonMappingException e) {
            throw new BankException(e.getMessage(), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                //Ignore
            }
        }

    }

    public String objectAsJson(Object value) throws BankException {
        try {
            return mObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BankException(e.getMessage(), e);
        }
    }

    protected abstract String getAppId();
}
