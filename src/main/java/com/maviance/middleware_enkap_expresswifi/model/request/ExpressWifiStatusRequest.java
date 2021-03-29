package com.maviance.middleware_enkap_expresswifi.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class ExpressWifiStatusRequest {
    @JsonProperty("payment_id")
    @NotBlank
    private String paymentId;
    @NotBlank
    private String hmac;
    @JsonProperty("partner_id")
    @NotBlank
    private String partnerId;
    @Min(1)
    private long timestamp;
}
