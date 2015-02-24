
package com.liato.bankdroid.banking.banks.coop.model.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {

    @JsonProperty("date")
    private String date;
    @JsonProperty("icon")
    private boolean icon;
    @JsonProperty("title")
    private String title;
    @JsonProperty("cardholder")
    private String cardholder;
    @JsonProperty("location")
    private String location;
    @JsonProperty("sum")
    private double sum;
    @JsonProperty("charity")
    private boolean charity;
    @JsonProperty("hasdetails")
    private boolean hasdetails;
    @JsonProperty("detailsurl")
    private String detailsurl;
    @JsonProperty("batchnumber")
    private int batchnumber;
    @JsonProperty("sequencenumber")
    private int sequencenumber;

    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    @JsonProperty("icon")
    public boolean getIcon() {
        return icon;
    }

    @JsonProperty("icon")
    public void setIcon(boolean icon) {
        this.icon = icon;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("cardholder")
    public String getCardholder() {
        return cardholder;
    }

    @JsonProperty("cardholder")
    public void setCardholder(String cardholder) {
        this.cardholder = cardholder;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("sum")
    public double getSum() {
        return sum;
    }

    @JsonProperty("sum")
    public void setSum(double sum) {
        this.sum = sum;
    }

    @JsonProperty("charity")
    public boolean getCharity() {
        return charity;
    }

    @JsonProperty("charity")
    public void setCharity(boolean charity) {
        this.charity = charity;
    }

    @JsonProperty("hasdetails")
    public boolean getHasdetails() {
        return hasdetails;
    }

    @JsonProperty("hasdetails")
    public void setHasdetails(boolean hasdetails) {
        this.hasdetails = hasdetails;
    }

    @JsonProperty("detailsurl")
    public String getDetailsurl() {
        return detailsurl;
    }

    @JsonProperty("detailsurl")
    public void setDetailsurl(String detailsurl) {
        this.detailsurl = detailsurl;
    }

    @JsonProperty("batchnumber")
    public int getBatchnumber() {
        return batchnumber;
    }

    @JsonProperty("batchnumber")
    public void setBatchnumber(int batchnumber) {
        this.batchnumber = batchnumber;
    }

    @JsonProperty("sequencenumber")
    public int getSequencenumber() {
        return sequencenumber;
    }

    @JsonProperty("sequencenumber")
    public void setSequencenumber(int sequencenumber) {
        this.sequencenumber = sequencenumber;
    }


}
