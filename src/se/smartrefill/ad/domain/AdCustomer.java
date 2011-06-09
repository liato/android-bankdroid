package se.smartrefill.ad.domain;

import java.io.Serializable;

import se.smartrefill.ad.bank.domain.AdBalanceService;

public class AdCustomer extends AdAbstractCustomer implements Serializable {
	private static final long serialVersionUID = 1L;

	public AdBalanceService getBalanceService() {
		
		for (AdService service : this.services) {
			if (service instanceof AdBalanceService)
				return (AdBalanceService)service;
		}
		return null;
	}
}
