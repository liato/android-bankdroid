package com.liato.bankdroid.banking.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class DEVID implements Serializable {

    private static final long serialVersionUID = -8706994448508325149L;

    @JsonProperty("APPLICATION_VERSION")
    private String applicationVersion;

    @JsonProperty("OS_NAME")
    private String osName;

    @JsonProperty("MODEL")
    private String model;

    @JsonProperty("MANUFACTURER")
    private String manufacturer;

    @JsonProperty("OS_VERSION")
    private String osVersion;

    @JsonProperty("APPLICATION_NAME")
    private String applicationName;


    public DEVID() {

    }

    public DEVID(String model, String applicationName, String applicationVersion, String osName,
            String osVersion, String manufacturer) {
        this.model = model;
        this.applicationName = applicationName;
        this.osVersion = osVersion;
        this.manufacturer = manufacturer;
        this.applicationVersion = applicationVersion;
        this.osName = osName;
    }

    public static DEVID createDefault() {
        return new DEVID("45", "MASP", "6.0.0", "Android", "5", "Apple");
    }

    @JsonProperty("APPLICATION_VERSION")
    public String getApplicationVersion() {
        return applicationVersion;
    }

    @JsonProperty("APPLICATION_VERSION")
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @JsonProperty("OS_NAME")
    public String getOsName() {
        return osName;
    }

    @JsonProperty("OS_NAME")
    public void setOsName(String osName) {
        this.osName = osName;
    }

    @JsonProperty("MODEL")
    public String getModel() {
        return model;
    }

    @JsonProperty("MODEL")
    public void setModel(String model) {
        this.model = model;
    }

    @JsonProperty("MANUFACTURER")
    public String getManufacturer() {
        return manufacturer;
    }

    @JsonProperty("MANUFACTURER")
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @JsonProperty("OS_VERSION")
    public String getOsVersion() {
        return osVersion;
    }

    @JsonProperty("OS_VERSION")
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @JsonProperty("APPLICATION_NAME")
    public String getApplicationName() {
        return applicationName;
    }

    @JsonProperty("APPLICATION_NAME")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


}
