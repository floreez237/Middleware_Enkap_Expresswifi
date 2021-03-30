package com.maviance.middleware_enkap_expresswifi.model.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.maviance.middleware_enkap_expresswifi.enums.ExpressWifiStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public abstract class CustomResponse {
    @NotNull(message = "A valid expressWifiStatus must be entered")
    @JsonProperty("status")
    protected ExpressWifiStatus expressWifiStatus;
}
