package com.maviance.middleware_enkap_expresswifi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class RedirectResponse extends CustomResponse {
    @JsonProperty("redirection_url")
    private String redirectUrl;
}
