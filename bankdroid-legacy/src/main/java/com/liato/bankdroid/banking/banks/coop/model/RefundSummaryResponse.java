package com.liato.bankdroid.banking.banks.coop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundSummaryResponse extends BaseResponse {

    @JsonProperty("RefundSummaryResult")
    private RefundSummaryResult refundSummaryResult;

    @JsonProperty("RefundSummaryResult")
    public RefundSummaryResult getRefundSummaryResult() {
        return refundSummaryResult;
    }

    @JsonProperty("RefundSummaryResult")
    public void setRefundSummaryResult(RefundSummaryResult refundSummaryResult) {
        this.refundSummaryResult = refundSummaryResult;
    }

}