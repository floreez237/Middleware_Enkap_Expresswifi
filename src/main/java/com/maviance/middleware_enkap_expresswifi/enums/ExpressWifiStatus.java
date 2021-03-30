package com.maviance.middleware_enkap_expresswifi.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExpressWifiStatus {
    @JsonProperty("success") SUCCESS,
    @JsonProperty("canceled") CANCELED,
    @JsonProperty("error") ERROR,
    @JsonProperty("declined") DECLINED,
    @JsonProperty("abandoned") ABANDONED,
    @JsonProperty("failure") FAILURE,
    @JsonProperty("pending") PENDING;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase();
    }
}
