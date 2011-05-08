package se.smartrefill.ad.bank.domain;

import java.io.Serializable;

public class AdCekabPanNotificationLogMedia implements Serializable {
	private static final long serialVersionUID = 1L;
	private String caption;
	private int cekabPanNotificationLogId;
	private int id;
	private byte[] media;
	private String mediaType;

	public String getCaption() {
		return caption;
	}

	public int getCekabPanNotificationLogId() {
		return cekabPanNotificationLogId;
	}

	public int getId() {
		return id;
	}

	public byte[] getMedia() {
		return media;
	}

	public String getMediaType() {
		return mediaType;
	}
}
