package com.liato.bankdroid.banking.banks.nordea.api.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.liato.bankdroid.banking.banks.nordea.api.model.AuthenticationToken;
import com.liato.bankdroid.util.ObjectUtils;

@JacksonXmlRootElement(localName = "lightLoginResponse")
public class LightLoginResponse extends AbstractResponse {

    private AuthenticationToken mAuthenticationToken;

    public void setAuthenticationToken(AuthenticationToken pAuthenticationToken) {
        mAuthenticationToken = pAuthenticationToken;
    }

    @JacksonXmlProperty(localName = "authenticationToken")
    public AuthenticationToken getAuthenticationToken() {
        return mAuthenticationToken;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LightLoginResponse other = (LightLoginResponse) obj;
        return super.equals(obj) && ObjectUtils.equal(this.mAuthenticationToken, other.mAuthenticationToken);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + ObjectUtils.hashCode(mAuthenticationToken);
    }
}
