package com.maviance.middleware_enkap_expresswifi.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestSource {
    @JsonProperty("customer") CUSTOMER,
    @JsonProperty("retailer") RETAILER,
    @JsonProperty("unknown") UNKNOWN;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase();
    }
}
