package se.smartrefill.ad.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AdAbstractCustomer
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  protected String countryCode;
  protected String customerOwner;
  protected Integer id;
  protected String maskedCardNumber;
  protected Boolean mustChangePassword;
  protected Integer paymentCardId;
  protected List<AdPaymentCard> paymentCards;
  protected List<AdService> services;
  protected String socialSecurityNumber;

  public AdAbstractCustomer()
  {
    this.services = new ArrayList<AdService>();
    this.paymentCards = new ArrayList<AdPaymentCard>();
  }
  
  public int getId(){
	  return id;
  }
}
