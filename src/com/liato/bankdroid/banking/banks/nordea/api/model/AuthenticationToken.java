package com.liato.bankdroid.banking.banks.nordea.api.model;

import java.util.Date;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.liato.bankdroid.util.ObjectUtils;

public class AuthenticationToken {

    private String mToken;

    private int mAuthLevel;

    private Date mLoginTime;

    private Date mNotAfter;

    private int mSessionMaxLength;

    private int mTokenMaxAge;

    private String mProfileId;

    /**
     * @return the mToken
     */
    @JacksonXmlProperty(localName = "token")
    public String getToken() {
        return mToken;
    }

    /**
     * @param mToken
     *            the mToken to set
     */
    public void setToken(String pToken) {
        this.mToken = pToken;
    }

    /**
     * @return the mAuthLevel
     */
    @JacksonXmlProperty(localName = "authLevel")
    public int getAuthLevel() {
        return mAuthLevel;
    }

    /**
     * @param mAuthLevel
     *            the mAuthLevel to set
     */
    public void setAuthLevel(int pAuthLevel) {
        this.mAuthLevel = pAuthLevel;
    }

    /**
     * @return the mLoginTime
     */
    @JacksonXmlProperty(localName = "loginTime")
    public Date getLoginTime() {
        return mLoginTime;
    }

    /**
     * @param mLoginTime
     *            the mLoginTime to set
     */
    public void setLoginTime(Date pLoginTime) {
        this.mLoginTime = pLoginTime;
    }

    /**
     * @return the mNotAfter
     */
    @JacksonXmlProperty(localName = "notAfter")
    public Date getNotAfter() {
        return mNotAfter;
    }

    /**
     * @param mNotAfter
     *            the mNotAfter to set
     */
    public void setNotAfter(Date pNotAfter) {
        this.mNotAfter = pNotAfter;
    }

    /**
     * @return the mSessionMaxLength
     */
    @JacksonXmlProperty(localName = "sessionMaxLength")
    public int getSessionMaxLength() {
        return mSessionMaxLength;
    }

    /**
     * @param mSessionMaxLength
     *            the mSessionMaxLength to set
     */
    public void setSessionMaxLength(int pSessionMaxLength) {
        this.mSessionMaxLength = pSessionMaxLength;
    }

    /**
     * @return the mTokenMaxLength
     */
    @JacksonXmlProperty(localName = "tokenMaxAge")
    public int getTokenMaxAge() {
        return mTokenMaxAge;
    }

    /**
     * @param mTokenMaxLength
     *            the mTokenMaxLength to set
     */
    public void setTokenMaxAge(int pTokenMaxAge) {
        this.mTokenMaxAge = pTokenMaxAge;
    }

    /**
     * @return the mProfileId
     */
    @JacksonXmlProperty(localName = "profileId")
    public String getProfileId() {
        return mProfileId;
    }

    /**
     * @param mProfileId
     *            the mProfileId to set
     */
    public void setProfileId(String pProfileId) {
        this.mProfileId = pProfileId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AuthenticationToken other = (AuthenticationToken) obj;
        return ObjectUtils.equal(this.mAuthLevel, other.mAuthLevel)
                && ObjectUtils.equal(this.mLoginTime, other.mLoginTime)
                && ObjectUtils.equal(this.mNotAfter, other.mNotAfter)
                && ObjectUtils.equal(this.mProfileId, other.mProfileId)
                && ObjectUtils.equal(this.mSessionMaxLength, other.mSessionMaxLength)
                && ObjectUtils.equal(this.mToken, other.mToken)
                && ObjectUtils.equal(this.mTokenMaxAge, other.mTokenMaxAge);
    }
    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(this.mAuthLevel,
                this.mLoginTime,
                this.mNotAfter,
                this.mProfileId,
                this.mSessionMaxLength,
                this.mToken,
                this.mTokenMaxAge);
    }
}
