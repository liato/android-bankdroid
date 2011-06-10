package se.smartrefill.ad.domain;

import java.io.Serializable;

public class AdPaymentCard
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String description;
  private int id;
  private String maskedPaymentCard;
  private int paymentRef;
  
  public String getDescription() {
		return description;
	}
	public int getId() {
		return id;
	}
	public String getMaskedPaymentCard() {
		return maskedPaymentCard;
	}
	public int getPaymentRef() {
		return paymentRef;
	}
}
