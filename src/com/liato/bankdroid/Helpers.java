package com.liato.bankdroid;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Helpers {
	public static BigDecimal parseBalance(String balance) {
		balance = balance.replaceAll("(?:\\.|&nbsp;| )*", "");
		balance = balance.replace(",", ".");
		return new BigDecimal(balance);
	}
	public static String formatBalance(BigDecimal balance) {
		Locale locale = new Locale("sv", "SE");
		return NumberFormat.getCurrencyInstance(locale).format(balance).replace("kr", "SEK");
	}
}
