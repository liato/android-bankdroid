package se.smartrefill.ad.bank.remoting.service;

import java.util.List;
import se.smartrefill.ad.bank.domain.AdAccount;

public abstract interface RemoteBalanceService
{
  public abstract AdAccount getAccountTransactions(int customerId, AdAccount account, String scrollDirection);
  public abstract List<AdAccount> getAccounts(int customerId);
}