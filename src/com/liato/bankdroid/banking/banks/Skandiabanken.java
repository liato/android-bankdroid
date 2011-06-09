package com.liato.bankdroid.banking.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.smartrefill.ad.bank.domain.AdAccount;
import se.smartrefill.ad.bank.domain.AdBalanceInformationTransaction;
import se.smartrefill.ad.bank.remoting.service.RemoteBalanceService;
import se.smartrefill.ad.bank.remoting.service.RemoteCustomerService;
import se.smartrefill.ad.domain.AdCustomer;
import se.smartrefill.exception.CustomerBlockedException;
import se.smartrefill.exception.InvalidLoginException;
import se.smartrefill.exception.InvalidSecurityCodeException;
import se.smartrefill.exception.UnauthenticatedUserException;

import android.content.Context;
import android.text.InputType;

import com.caucho.hessian.client.HessianProxyFactory;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Skandiabanken extends Bank {
	private static final String BASE_URL = "https://smartrefill.se/mobile/bank/android/";
	private static final String LOGIN_URL = BASE_URL + "customer.service";
	private static final String BALANCE_URL = BASE_URL + "balance.service";
	
	private final static String customerOwner = "SKANDIABANKEN";
	private final static String countryCode = "SE";
	
	// Does not seem to matter, should perhaps be the password of the user
	private final static String securityCode = "unknown"; 
	private int customerBalanceServiceId = 0;

	public Skandiabanken(Context context) {
		super(context);
		TAG = "Skandiabanken";
		NAME = "Skandiabanken";
		NAME_SHORT = "Skandiabanken";
		BANKTYPE_ID = IBankTypes.SKANDIABANKEN;
		;
		URL = "http://www.skandiabanken.se/hem/";
		INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
		INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
		INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
	}

	public Skandiabanken(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {

		try {
			RemoteCustomerService test = (RemoteCustomerService) getHessianProxy()
					.create(RemoteCustomerService.class, LOGIN_URL, context.getClassLoader());
			AdCustomer customer = test.login(username, password, customerOwner,
					countryCode);
			customerBalanceServiceId = customer.getBalanceService().getId();
		} catch (InvalidLoginException e) {
			throw new LoginException(res.getText(
					R.string.invalid_username_password).toString());
		} catch (InvalidSecurityCodeException e) {
			throw new LoginException(res.getText(
					R.string.invalid_username_password).toString());
		} catch (UnauthenticatedUserException e) {
			throw new LoginException(res.getText(
					R.string.invalid_username_password).toString());
		} catch (CustomerBlockedException e) {
			// TODO other message!
			throw new LoginException(res.getText(
					R.string.invalid_username_password).toString());
		} catch (IOException e) {
			throw new BankException(e.getMessage());
		}
		return urlopen;
	}

	@Override
	public void update() throws BankException, LoginException, BankChoiceException {
		super.update();

		if (username == null || password == null || username.length() == 0
				|| password.length() == 0)
			throw new LoginException(res.getText(
					R.string.invalid_username_password).toString());

		login();

		try {
			RemoteBalanceService balanceService = (RemoteBalanceService) getHessianProxy()
					.create(RemoteBalanceService.class, BALANCE_URL, context.getClassLoader());
			List<AdAccount> adAccounts = balanceService.getAccounts(
					customerBalanceServiceId, securityCode);
			for (AdAccount adAccount : adAccounts) {
				String amount = adAccount.getAmount();
				if (amount == null)
					continue;
				String typeDescription = adAccount.getAccountTypeDescription();
				if (typeDescription.contentEquals("Upplagt konto"))
					continue;
				
				int type = Account.OTHER;
				if (typeDescription.contentEquals("Allt i Ett-konto") || typeDescription.contentEquals("Sparkonto"))
					type = Account.REGULAR;
				
				accounts.add(new Account(adAccount.getAlias(), Helpers
						.parseBalance(amount), adAccount.getId(), type));
			}
		} catch (IOException e) {
			throw new BankException(e.getMessage());
		} finally {
			super.updateComplete();
		}
	}
	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

		try {
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			RemoteBalanceService balanceService = (RemoteBalanceService) getHessianProxy()
				.create(RemoteBalanceService.class, BALANCE_URL, context.getClassLoader());
			AdAccount adAccount = null;
			List<AdAccount> adAccounts = balanceService.getAccounts(customerBalanceServiceId, securityCode);
			for (AdAccount adAccounti : adAccounts) {
				if (adAccounti.getId().contentEquals(account.getId())){
					adAccount = adAccounti;
					break;
				}
			}
			// TODO Check null
			
			AdAccount accountWithTransactions =  balanceService.getAccountTransactions(adAccount, "f", securityCode);
    		List<AdBalanceInformationTransaction> adTransactions = accountWithTransactions.getTransactions();
    		for (AdBalanceInformationTransaction transaction : adTransactions) {
				String transactionAmount = transaction.getAmount();
				String time = transaction.getTime();
				String merchant = transaction.getMerchant();
				transactions.add(new Transaction(time, merchant, Helpers.parseBalance(transactionAmount)));	
			}
			account.setTransactions(transactions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private HessianProxyFactory getHessianProxy() {
		HessianProxyFactory localHessianProxyFactory = new HessianProxyFactory();
		localHessianProxyFactory.setHessian2Request(false);
		localHessianProxyFactory.setHessian2Reply(false);
		localHessianProxyFactory.setChunkedPost(false);
		localHessianProxyFactory.setReadTimeout(30000L);
		localHessianProxyFactory.setOverloadEnabled(false);
		return localHessianProxyFactory;
	}

}
