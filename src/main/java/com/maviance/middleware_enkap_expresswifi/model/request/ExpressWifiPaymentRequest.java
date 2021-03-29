package com.maviance.middleware_enkap_expresswifi.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maviance.middleware_enkap_expresswifi.enums.RequestSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class ExpressWifiPaymentRequest {
    private double amount;
    @NotBlank(message = "The currency must not be blank")
    private String currency;
    @NotBlank(message = "Hmac must not be blank")
    private String hmac;
    @Min(value = 1)
    private long timestamp;
    @JsonProperty("payment_id")
    @NotBlank
    private String paymentId;
    @URL(protocol = "https",message = "Must be a Valid HTTPS URL")
    @NotBlank
    @JsonProperty("callback_url")
    private String callbackUrl;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private RequestSource source; //can have the value customer, retailer or unknown


}
