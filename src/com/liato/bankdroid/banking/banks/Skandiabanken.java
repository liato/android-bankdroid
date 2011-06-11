package com.liato.bankdroid.banking.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.smartrefill.ad.bank.domain.AdAccount;
import se.smartrefill.ad.bank.domain.AdBalanceInformationTransaction;
import se.smartrefill.ad.bank.domain.AdLoginRequest;
import se.smartrefill.ad.bank.remoting.service.RemoteBalanceService;
import se.smartrefill.ad.bank.remoting.service.RemoteCustomerService;
import se.smartrefill.ad.domain.AdCustomer;
import se.smartrefill.remoting.domain.exception.CustomerBlockedException;
import se.smartrefill.remoting.domain.exception.InvalidLoginException;
import se.smartrefill.remoting.domain.exception.InvalidSecurityCodeException;
import se.smartrefill.remoting.domain.exception.UnauthenticatedUserException;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.InputType;

import com.caucho.hessian.client.HessianConnectionException;
import com.caucho.hessian.client.HessianHttpHeaderProxyFactory;
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
	private static final String BASE_URL = "https://smartrefill.se/mobile/bank/android2/";
	private static final String LOGIN_URL = BASE_URL + "customer.service";
	private static final String BALANCE_URL = BASE_URL + "balance.service";
	
	  private static final String HTTP_HEADER_SMARTREFILL_APPLICATION_ID = "x-smartrefill-application";
	  private static final String HTTP_HEADER_SMARTREFILL_APPLICATION_VERSION = "x-smartrefill-version";
	  private static final String HTTP_HEADER_SMARTREFILL_COUNTRY_CODE = "x-smartrefill-country-code";
	  private static final String HTTP_HEADER_SMARTREFILL_CUSTOMER_ID = "x-smartrefill-customer";
	  private static final String HTTP_HEADER_SMARTREFILL_CUSTOMER_OWNER = "x-smartrefill-customer-owner";
	  private static final String HTTP_HEADER_SMARTREFILL_DEVICE_ID = "x-smartrefill-device";
	  private static final String HTTP_HEADER_SMARTREFILL_INFLOW = "x-smartrefill-inflow";
	  private static final String HTTP_HEADER_SMARTREFILL_SECURITY_CODE = "x-smartrefill-security-code";
	  private static final String INFLOW_ANDROID = "Android";
	  private final static String customerOwner = "SKANDIABANKEN";
	  private final static String countryCode = "SE";
	
	private int customerId = 0;

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
			HessianHttpHeaderProxyFactory proxyFactory = getHessianProxy();
			
			RemoteCustomerService test = (RemoteCustomerService) proxyFactory
				.create(RemoteCustomerService.class, LOGIN_URL, context.getClassLoader());
			
			AdCustomer customer = test.login(new AdLoginRequest(username, password));
			customerId = customer.getId();
			proxyFactory.addHeader(HTTP_HEADER_SMARTREFILL_CUSTOMER_ID, String.valueOf(customerId));
			proxyFactory.addHeader(HTTP_HEADER_SMARTREFILL_SECURITY_CODE, password);
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
			// TODO hard coded string!
			throw new LoginException("User has been blocked.");
		} catch (IOException e) {
			throw new BankException(e.getMessage());
		} catch (HessianConnectionException e) {
			throw new BankException(e.getMessage());
		} 
		return urlopen;
	}
	
	public void logout() throws BankException {

		try {
			HessianHttpHeaderProxyFactory proxyFactory = getHessianProxy();
			
			RemoteCustomerService test = (RemoteCustomerService) proxyFactory
					.create(RemoteCustomerService.class, LOGIN_URL, context.getClassLoader());
			
			test.logout(customerId);
			customerId = 0;
			proxyFactory.removeHeader(HTTP_HEADER_SMARTREFILL_CUSTOMER_ID);
			proxyFactory.removeHeader(HTTP_HEADER_SMARTREFILL_SECURITY_CODE);
		} catch (IOException e) {
			throw new BankException(e.getMessage());
		} catch (HessianConnectionException e) {
			throw new BankException(e.getMessage());
		} catch (RuntimeException e) {
			throw new BankException("Unexpected error connecting to Skandiabanken: " + e.getMessage());
		}
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
			
			List<AdAccount> adAccounts = balanceService.getAccounts(customerId);
			
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
		} catch (RuntimeException e) {
			throw new BankException("Unexpected error getting balance from Skandiabanken: " + e.getMessage());
		} finally {
			super.updateComplete();
		}
		
		logout();
	}
	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

		try {
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			RemoteBalanceService balanceService = (RemoteBalanceService) getHessianProxy()
				.create(RemoteBalanceService.class, BALANCE_URL, context.getClassLoader());
			AdAccount adAccount = null;
			List<AdAccount> adAccounts = balanceService.getAccounts(customerId);
			for (AdAccount adAccounti : adAccounts) {
				if (adAccounti.getId().contentEquals(account.getId())){
					adAccount = adAccounti;
					break;
				}
			}
			// TODO Check null
			
			AdAccount accountWithTransactions =  balanceService.getAccountTransactions(customerId, adAccount, "f");
    		List<AdBalanceInformationTransaction> adTransactions = accountWithTransactions.getTransactions();
    		for (AdBalanceInformationTransaction transaction : adTransactions) {
				String transactionAmount = transaction.getAmount();
				String time = transaction.getTime();
				String merchant = transaction.getMerchant();
				transactions.add(new Transaction(time, merchant, Helpers.parseBalance(transactionAmount)));	
			}
			account.setTransactions(transactions);
		} catch (IOException e) {
			throw new BankException(e.getMessage());
		} catch (RuntimeException e) {
			throw new BankException("Unexpected error getting transactions from Skandiabanken: " + e.getMessage());
		}
	}
	
	private HessianHttpHeaderProxyFactory mProxyFactory = null;

	private HessianHttpHeaderProxyFactory getHessianProxy() {
		if (mProxyFactory == null)
		{
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put(HTTP_HEADER_SMARTREFILL_APPLICATION_ID, "se.skandiabanken.android.wallet");
			headers.put(HTTP_HEADER_SMARTREFILL_APPLICATION_VERSION, "6");
			headers.put(HTTP_HEADER_SMARTREFILL_COUNTRY_CODE, countryCode);
			headers.put(HTTP_HEADER_SMARTREFILL_CUSTOMER_OWNER, customerOwner);
			headers.put(HTTP_HEADER_SMARTREFILL_DEVICE_ID, getDeviceId());
			headers.put(HTTP_HEADER_SMARTREFILL_INFLOW, INFLOW_ANDROID);
			
			HessianHttpHeaderProxyFactory localHessianProxyFactory = 
				new HessianHttpHeaderProxyFactory(headers);
			
			localHessianProxyFactory.setHessian2Request(false);
			localHessianProxyFactory.setHessian2Reply(false);
			localHessianProxyFactory.setChunkedPost(false);
			localHessianProxyFactory.setReadTimeout(30000L);
			localHessianProxyFactory.setOverloadEnabled(false);
			mProxyFactory = localHessianProxyFactory;
		}
		
		return mProxyFactory;
	}
	
	  public String getDeviceId()
	  {
//		    TelephonyManager localTelephonyManager = (TelephonyManager)context.getSystemService("phone");
//			if (localTelephonyManager.getDeviceId() != null) // null for emulator
//				return localTelephonyManager.getDeviceId();
//			else
//				return "000000000000000"; 
		  // We should return the imei of the phone (se code above)
		  // As we would need permission to read imei we use something else that is unique and constant
		  // Bankdroid should have as less permissions as possible...
		  String test = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		  
		  if (test == null) // null for emulator
			  test = "0";
		  
		  // convert to decimal string (imei is decimal)
		  try{
			  test = String.valueOf(Integer.parseInt(test, 16));
		  } catch (NumberFormatException e) {}
		  
		  while (test.length() < 16)
			  test += "0";
		  
		  return test.substring(0, 15);
	  }
}
