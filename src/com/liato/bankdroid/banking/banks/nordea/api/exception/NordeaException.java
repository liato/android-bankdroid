package com.liato.bankdroid.banking.banks.nordea.api.exception;


public class NordeaException extends Exception {

    public NordeaException(String message) {
        super(message);
    }
    public NordeaException() {
        super();
    }
    
	private static final long serialVersionUID = -1780388045254680275L;
	
	private String mHttpResponse;
	
	public String getHttpResponse() {
		return mHttpResponse;
	}
	
	public void setHttpResponse(String pHttpResponse) {
		mHttpResponse = pHttpResponse;
	}
}
