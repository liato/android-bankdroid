package com.liato.bankdroid;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Helpers {
	public static BigDecimal parseBalance(String balance) {
		//balance = balance.replaceAll("(?:\\.|&nbsp;| )*", "");
		//balance = balance.replaceAll("[a-zA-Z]*", "");
		//balance = balance.replaceAll("\\s*", "");
		balance = balance.replaceAll("[^0-9,.-]*", "");
		balance = balance.replace(",", ".");
		if (balance.indexOf(".") != balance.lastIndexOf(".")) {
			String b = balance.substring(balance.lastIndexOf("."));
			balance = balance.substring(0, balance.lastIndexOf("."));
			balance = balance.replace(".", "");
			balance = balance+b;
		}
		return new BigDecimal(balance);
	}
	public static String formatBalance(BigDecimal balance) {
		Locale locale = new Locale("sv", "SE");
		String bs = NumberFormat.getCurrencyInstance(locale).format(balance);
		bs = bs.replaceAll("[^0-9\\s,.-]*", "");
		return bs.trim() + " SEK";
	}
	public static String formatBalance(Double balance) {
		return formatBalance(new BigDecimal(balance));
	}
}
