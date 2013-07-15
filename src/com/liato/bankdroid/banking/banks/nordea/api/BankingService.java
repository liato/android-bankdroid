package com.liato.bankdroid.banking.banks.nordea.api;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.github.kevinsawicki.http.HttpRequest;
import com.liato.bankdroid.banking.banks.nordea.api.exception.NordeaException;
import com.liato.bankdroid.banking.banks.nordea.api.model.AuthenticationToken;
import com.liato.bankdroid.banking.banks.nordea.api.model.NordeaAccount;
import com.liato.bankdroid.banking.banks.nordea.api.model.NordeaTransaction;
import com.liato.bankdroid.banking.banks.nordea.api.model.response.GetAccountTransactionDetailsOut;
import com.liato.bankdroid.banking.banks.nordea.api.model.response.GetAccountTransactionsOut;
import com.liato.bankdroid.banking.banks.nordea.api.model.response.GetInitialContextOut;

public class BankingService extends AbstractNordeaService {

	private static final String API_URL = "https://mobilebankingservices.nordea.com/BankingServiceV1.1/";
	
	public BankingService(AuthenticationToken pAuthenticationToken) {
		super();
		super.setAuthenticationToken(pAuthenticationToken);
	}
	
	public BankingService() {
	    super();
	}
	/**
	 * List all available accounts with account details.
	 * @return
	 * @throws NordeaException 
	 */
	public GetInitialContextOut getInitialContext() throws NordeaException {
		HttpRequest vHttpRequest = HttpRequest.get(API_URL+"initialContext");
		return request(vHttpRequest,GetInitialContextOut.class);
	}
	
	/**
	 * List all transactions for the given account.
	 * @param account The account where the transactions should be taken from.
	 * @return A list of all transactions for the given account.
	 * @throws NordeaException 
	 */
	public GetAccountTransactionsOut getAccountTransactions(NordeaAccount account) throws NordeaException {
		
		try {
			HttpRequest vHttpRequest = HttpRequest.get(API_URL+"Transactions",true,"accountId",URLEncoder.encode(account.getAccoundId().getId(),"utf-8"));
			return request(vHttpRequest,GetAccountTransactionsOut.class);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get details for a given transaction.
	 * @param transaction The transaction to get details for.
	 * @return Details about the transaction.
	 * @throws NordeaException 
	 */
	public GetAccountTransactionDetailsOut getAccountTransactionDetails(NordeaTransaction transaction) throws NordeaException{
		HttpRequest vHttpRequest = HttpRequest.get(API_URL+"Transactions/"+transaction.getTransactionKey());
		return request(vHttpRequest,GetAccountTransactionDetailsOut.class);
	}
}
