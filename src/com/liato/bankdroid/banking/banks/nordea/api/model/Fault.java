package com.liato.bankdroid.banking.banks.nordea.api.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Fault {
	
	private String mFaultCode;

	private String mFaultString;
	
	@JacksonXmlProperty(localName="faultcode")
	public String getFaultCode() {
		return mFaultCode;
	}
	public void setFaultcode(String pFaultCode) {
		mFaultCode = pFaultCode;
	}
	
	@JacksonXmlProperty(localName="faultstring")
	public String getFaultString() {
		return mFaultString;	
	}
	
	public void setFaultString(String pFaultString) {
		mFaultString = pFaultString;
	}
}
