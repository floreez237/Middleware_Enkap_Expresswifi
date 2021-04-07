package com.maviance.middleware_enkap_expresswifi.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This Enumerator provides the Different type of source that we can get from Express Wifi.
 *
 * @author Florian Lowe
 */
public enum RequestSource {
    @JsonProperty("customer") CUSTOMER,
    @JsonProperty("retailer") RETAILER,
    @JsonProperty("unknown") UNKNOWN;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase();
    }
}
