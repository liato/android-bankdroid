package com.liato.bankdroid.banking.banks.coop.model.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Model {

    @JsonProperty("results")
    private List<Result> results = new ArrayList<Result>();

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("id")
    private String id;

    @JsonProperty("page")
    private int page;

    @JsonProperty("pageCount")
    private int pageCount;

    @JsonProperty("pageSize")
    private int pageSize;

    @JsonProperty("results")
    public List<Result> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<Result> results) {
        this.results = results;
    }

    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(String from) {
        this.from = from;
    }

    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("page")
    public int getPage() {
        return page;
    }

    @JsonProperty("page")
    public void setPage(int page) {
        this.page = page;
    }

    @JsonProperty("pageCount")
    public int getPageCount() {
        return pageCount;
    }

    @JsonProperty("pageCount")
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @JsonProperty("pageSize")
    public int getPageSize() {
        return pageSize;
    }

    @JsonProperty("pageSize")
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
