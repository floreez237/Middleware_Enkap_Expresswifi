package com.maviance.middleware_enkap_expresswifi.exceptions;

import com.maviance.middleware_enkap_expresswifi.model.response.ErrorResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ExpressWifiException extends RuntimeException {
    private ErrorResponse errorResponse;

    public ExpressWifiException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
