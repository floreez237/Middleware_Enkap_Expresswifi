package com.maviance.middleware_enkap_expresswifi.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ENkapOrderResponse {
    private String orderTransactionId;
    private String merchantReferenceId;
    @URL(protocol = "https", message = "The must use https")
    @NotBlank(message = "an URL must be entered")
    private String redirectUrl;

}
