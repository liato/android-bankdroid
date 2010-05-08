package com.liato.bankdroid;

public enum Banks {
	SWEDBANK 	("Swedbank"),
	NORDEA		("Nordea"),
	ICA			("ICA");

	private String value;
	private Banks(String value) {
		this.value = value;
	}
	public String toString() {
		return value;
	}
	public String getId() {
		return value.toLowerCase();
	}
}
