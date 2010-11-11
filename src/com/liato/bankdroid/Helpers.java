package com.liato.bankdroid;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import android.app.Activity;
import android.util.Log;

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
		BigDecimal ret;
		try {
			ret = new BigDecimal(balance);
		}
		catch (NumberFormatException e) {
			ret = new BigDecimal(0);
		}
		return ret;
	}
	public static String formatBalance(BigDecimal balance, String curr) {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(',');
		dfs.setGroupingSeparator(' ');
		DecimalFormat currency = new DecimalFormat("#,##0.00 ");
		currency.setDecimalFormatSymbols(dfs);
		return currency.format(balance.doubleValue())+curr;
	}
	public static String formatBalance(Double balance, String curr) {
		return formatBalance(new BigDecimal(balance), curr);
	}
	
	public static void slowDebug(String TAG, String text) {
	    for (String s : text.split("\n")) {
	        Log.d(TAG, s);
	        try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
	    }
	}
	
	static public void setActivityAnimation(Activity activity, int in, int out) {
	    try {
	        Method method = Activity.class.getMethod("overridePendingTransition", new Class[]{int.class, int.class});
	        method.invoke(activity, in, out);
	    } catch (Exception e) {
	        // Can't change animation, so do nothing
	    }
	}
	
}
