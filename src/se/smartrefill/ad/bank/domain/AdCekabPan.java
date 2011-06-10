package se.smartrefill.ad.bank.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import se.smartrefill.ad.domain.AdPaymentCard;

public class AdCekabPan implements Serializable {
	private static final long serialVersionUID = 1L;
	private int alertMeLowLimit;
	private List<AdCekabPanNotificationLog> cekabPanNotificationLogs;
	private Integer id;
	private AdPaymentCard paymentCard;
	private String status;

	public AdCekabPan() {
		this.cekabPanNotificationLogs = new ArrayList<AdCekabPanNotificationLog>();
	}

	public int getAlertMeLowLimit() {
		return alertMeLowLimit;
	}

	public List<AdCekabPanNotificationLog> getCekabPanNotificationLogs() {
		return cekabPanNotificationLogs;
	}

	public Integer getId() {
		return id;
	}

	public AdPaymentCard getPaymentCard() {
		return paymentCard;
	}

	public String getStatus() {
		return status;
	}
}