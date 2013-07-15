package com.liato.bankdroid.banking.banks.nordea.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.liato.bankdroid.banking.banks.nordea.api.model.ErrorMessage;
import com.liato.bankdroid.util.ObjectUtils;

public abstract class AbstractResponse {

	private ErrorMessage mErrorMessage;
	
	@JsonProperty("errorMessage")
	public ErrorMessage getErrorMessage(){
		return mErrorMessage;
	}
	
	@JsonSetter("errorMessage")
	public void setErrorMessage(ErrorMessage pErrorMessage) {
		mErrorMessage = pErrorMessage;
	}
	
	
@Override
public boolean equals(Object obj) {
    if(obj == null) {
        return false;
    }
    if(getClass() != obj.getClass()) {
        return false;
    }
    AbstractResponse other = (AbstractResponse) obj;
    return ObjectUtils.equal(this.mErrorMessage, other.mErrorMessage);
}

@Override
public int hashCode() {
    return ObjectUtils.hashCode(this.mErrorMessage);
}
}
