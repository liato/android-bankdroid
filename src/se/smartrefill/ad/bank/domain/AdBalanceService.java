package se.smartrefill.ad.bank.domain;

import java.util.ArrayList;
import java.util.List;
import se.smartrefill.ad.domain.AdService;

public class AdBalanceService extends AdService {
	private static final long serialVersionUID = 1L;
	private List<AdBalanceServiceCard> balanceServiceCards;
	private int dayOfMonthForBalance;
	private int dayOfWeekForBalance;
	private int endHourForBalance;
	private boolean monthlySubscription;
	private int startHourForBalance;
	private boolean weeklySubscription;

	public AdBalanceService() {
		this.balanceServiceCards = new ArrayList<AdBalanceServiceCard>();
	}

	public List<AdBalanceServiceCard> getBalanceServiceCards() {
		return balanceServiceCards;
	}

	public int getDayOfMonthForBalance() {
		return dayOfMonthForBalance;
	}

	public int getDayOfWeekForBalance() {
		return dayOfWeekForBalance;
	}

	public int getEndHourForBalance() {
		return endHourForBalance;
	}

	public boolean isMonthlySubscription() {
		return monthlySubscription;
	}

	public int getStartHourForBalance() {
		return startHourForBalance;
	}

	public boolean isWeeklySubscription() {
		return weeklySubscription;
	}
}