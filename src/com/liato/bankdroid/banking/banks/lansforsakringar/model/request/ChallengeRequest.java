package com.liato.bankdroid.banking.banks.lansforsakringar.model.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChallengeRequest {
    private int mOriginalChallenge;
    private String mChallengePair;
    private String mHash;


    public ChallengeRequest (int originalChallenge, String challengePair, String hash) {
        mOriginalChallenge = originalChallenge;
        mChallengePair = challengePair;
        mHash = hash;
    }

    @JsonSetter("originalChallenge")
    public void setOriginalChallenge(int o) { mOriginalChallenge = o; }
    @JsonProperty("originalChallenge")
    public int getOriginalChallenge() { return mOriginalChallenge; }

    @JsonSetter("challengePair")
    public void setChallengePair(String c) { mChallengePair = c; }
    @JsonProperty("challengePair")
    public String getChallengePair() { return mChallengePair; }

    @JsonSetter("hash")
    public void setHash(String h) { mHash = h; }
    @JsonProperty("hash")
    public String getHash() { return mHash; }

}