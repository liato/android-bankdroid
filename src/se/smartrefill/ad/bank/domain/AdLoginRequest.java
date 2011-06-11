package se.smartrefill.ad.bank.domain;

import java.io.Serializable;

public class AdLoginRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String password;
	private String username;

	public AdLoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String userName) {
		this.username = userName;
	}
}