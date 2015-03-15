package com.liato.bankdroid.banking.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class HWINFO implements Serializable {

    @JsonProperty("LONGITUDE_DECIMAL")
    private String longitudeDecimal;

    @JsonProperty("LATITUDE_DECIMAL")
    private String latitudeDecimal;

    @JsonProperty("COUNTRY_PREFIX")
    private long countryPrefix;

    public HWINFO() {

    }

    public HWINFO(long countryPrefix, String latitudeDecimal, String longitudeDecimal) {
        this.countryPrefix = countryPrefix;
        this.latitudeDecimal = latitudeDecimal;
        this.longitudeDecimal = longitudeDecimal;
    }

    public static HWINFO createDefault() {
        return new HWINFO(0, "0", "0");
    }

    @JsonProperty("LONGITUDE_DECIMAL")
    public String getLongitudeDecimal() {
        return longitudeDecimal;
    }

    @JsonProperty("LONGITUDE_DECIMAL")
    public void setLongitudeDecimal(String longitudeDecimal) {
        this.longitudeDecimal = longitudeDecimal;
    }

    @JsonProperty("LATITUDE_DECIMAL")
    public String getLatitudeDecimal() {
        return latitudeDecimal;
    }

    @JsonProperty("LATITUDE_DECIMAL")
    public void setLatitudeDecimal(String latitudeDecimal) {
        this.latitudeDecimal = latitudeDecimal;
    }

    @JsonProperty("COUNTRY_PREFIX")
    public long getCountryPrefix() {
        return countryPrefix;
    }

    @JsonProperty("COUNTRY_PREFIX")
    public void setCountryPrefix(long countryPrefix) {
        this.countryPrefix = countryPrefix;
    }

}
