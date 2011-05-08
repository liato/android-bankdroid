package se.smartrefill.ad.bank.domain;

import java.util.ArrayList;
import java.util.List;
import se.smartrefill.ad.domain.AdService;

public class AdMoneyTransferService extends AdService {
	private static final long serialVersionUID = 1L;
	private List<AdMoneyTransferCard> moneyTransferCards;

	public AdMoneyTransferService() {
		this.moneyTransferCards = new ArrayList<AdMoneyTransferCard>();
	}

	public List<AdMoneyTransferCard> getMoneyTransferCards() {
		return moneyTransferCards;
	}
}
