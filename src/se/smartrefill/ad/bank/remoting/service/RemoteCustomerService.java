package se.smartrefill.ad.bank.remoting.service;

import se.smartrefill.ad.bank.domain.AdLoginRequest;
import se.smartrefill.ad.domain.AdCustomer;

public abstract interface RemoteCustomerService
{
  public abstract AdCustomer login(AdLoginRequest paramAdLoginRequest);
  
  public abstract void logout(int customerId);
}
