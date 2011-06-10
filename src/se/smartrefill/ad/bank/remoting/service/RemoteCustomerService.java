package se.smartrefill.ad.bank.remoting.service;

import se.smartrefill.ad.domain.AdCustomer;
import se.smartrefill.exception.UnauthenticatedUserException;

public abstract interface RemoteCustomerService
{
  public abstract AdCustomer getCustomer(String username, String password, String customerOwner, String countryCode)
    throws UnauthenticatedUserException;

  public abstract AdCustomer getCustomerWithoutBalanceInformation(String paramString1, String paramString2, String paramString3, String paramString4)
    throws UnauthenticatedUserException;

  public abstract AdCustomer login(String username, String password, String customerOwner, String countryCode)
    throws UnauthenticatedUserException;

}