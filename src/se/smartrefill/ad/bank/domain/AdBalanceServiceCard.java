package se.smartrefill.ad.bank.domain;

import java.io.Serializable;
import se.smartrefill.ad.domain.AdPaymentCard;

public class AdBalanceServiceCard
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private AdBalanceInformation balanceInformation;
  private int dayOfMonthForBalance;
  private int dayOfWeekForBalance;
  private Integer id;
  private int monthlyBalanceEndHour;
  private int monthlyBalanceStartHour;
  private boolean monthlySubscription;
  private AdPaymentCard paymentCard;
  private int weeklyBalanceEndHour;
  private int weeklyBalanceStartHour;
  private boolean weeklySubscription;

  public AdBalanceInformation getBalanceInformation()
  {
    return this.balanceInformation;
  }

  public int getDayOfMonthForBalance()
  {
    return this.dayOfMonthForBalance;
  }

  public int getDayOfWeekForBalance()
  {
    return this.dayOfWeekForBalance;
  }

  public Integer getId()
  {
    return this.id;
  }

  public int getMonthlyBalanceEndHour()
  {
    return this.monthlyBalanceEndHour;
  }

  public int getMonthlyBalanceStartHour()
  {
    return this.monthlyBalanceStartHour;
  }

  public AdPaymentCard getPaymentCard()
  {
    return this.paymentCard;
  }

  public int getWeeklyBalanceEndHour()
  {
    return this.weeklyBalanceEndHour;
  }

  public int getWeeklyBalanceStartHour()
  {
    return this.weeklyBalanceStartHour;
  }

  public boolean isMonthlySubscription()
  {
    return this.monthlySubscription;
  }

  public boolean isWeeklySubscription()
  {
    return this.weeklySubscription;
  }
}