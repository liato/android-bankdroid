package se.smartrefill.ad.bank.remoting.service;

import java.util.List;
import se.smartrefill.ad.bank.domain.AdAccount;

public abstract interface RemoteBalanceService
{
  public abstract AdAccount getAccountTransactions(AdAccount account, String scrollDirection, String securityCode);
  public abstract List<AdAccount> getAccounts(int customerBalanceServiceId, String securityCode);
}