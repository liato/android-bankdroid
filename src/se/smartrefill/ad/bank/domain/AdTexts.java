package se.smartrefill.ad.bank.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AdTexts implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, String> texts;

	public AdTexts() {
		this.texts = new HashMap<String, String>();
	}

	public Map<String, String> getTexts() {
		return texts;
	}
}
