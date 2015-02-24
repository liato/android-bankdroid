package com.liato.bankdroid.banking.banks.coop.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebAuthenticateRequest {
    @JsonProperty("methodName")
    private String methodName = "Login";
    @JsonProperty("pageGuid")
    private String pageGuid;
    @JsonProperty("data")
    private Data data;

    public WebAuthenticateRequest(String pageGuid, String username, String password) {
        this.pageGuid = pageGuid;
        data = new Data(username, password);
    }

    @JsonProperty("methodName")
    public String getMethodName() {
        return methodName;
    }

    @JsonProperty("methodName")
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @JsonProperty("pageGuid")
    public String getPageGuid() {
        return pageGuid;
    }

    @JsonProperty("pageGuid")
    public void setPageGuid(String pageGuid) {
        this.pageGuid = pageGuid;
    }

    @JsonProperty("data")
    public Data getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(Data data) {
        this.data = data;
    }


    public static class Data {

        @JsonProperty("username")
        private String username;
        @JsonProperty("password")
        private String password;

        public Data(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @JsonProperty("username")
        public String getUsername() {
            return username;
        }

        @JsonProperty("username")
        public void setUsername(String username) {
            this.username = username;
        }

        @JsonProperty("password")
        public String getPassword() {
            return password;
        }

        @JsonProperty("password")
        public void setPassword(String password) {
            this.password = password;
        }

    }

}