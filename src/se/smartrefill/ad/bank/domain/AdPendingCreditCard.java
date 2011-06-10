package se.smartrefill.ad.bank.domain;

import java.io.Serializable;

public class AdPendingCreditCard
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String alias;
  private String number;
  private String partner;
  private String phoneNumber;
	private int serviceId;

	public String getAlias() {
		return alias;
	}

	public String getNumber() {
		return number;
	}

	public String getPartner() {
		return partner;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public int getServiceId() {
		return serviceId;
	}
}
