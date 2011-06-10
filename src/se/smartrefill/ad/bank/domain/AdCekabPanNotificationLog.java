package se.smartrefill.ad.bank.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdCekabPanNotificationLog implements Serializable {
	private static final long serialVersionUID = 1L;
	private String amount;
	private String city;
	private Date created;
	private String currency;
	private String functionCode;
	private Integer id;
	private transient String last4PaymentCardDigits;
	private String location;
	private List<AdCekabPanNotificationLogMedia> medias;
	private String pan;
	private String purchaseDate;
	private String purchaseTime;
	private String replyCode;
	private String result;
	private String shop;

	public AdCekabPanNotificationLog() {
		this.medias = new ArrayList<AdCekabPanNotificationLogMedia>();
		this.last4PaymentCardDigits = "";
	}

	public String getAmount() {
		return amount;
	}

	public String getCity() {
		return city;
	}

	public Date getCreated() {
		return created;
	}

	public String getCurrency() {
		return currency;
	}

	public String getFunctionCode() {
		return functionCode;
	}

	public Integer getId() {
		return id;
	}

	public String getLast4PaymentCardDigits() {
		return last4PaymentCardDigits;
	}

	public String getLocation() {
		return location;
	}

	public List<AdCekabPanNotificationLogMedia> getMedias() {
		return medias;
	}

	public String getPan() {
		return pan;
	}

	public String getPurchaseDate() {
		return purchaseDate;
	}

	public String getPurchaseTime() {
		return purchaseTime;
	}

	public String getReplyCode() {
		return replyCode;
	}

	public String getResult() {
		return result;
	}

	public String getShop() {
		return shop;
	}
}
