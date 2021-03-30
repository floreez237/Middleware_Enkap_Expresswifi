package com.maviance.middleware_enkap_expresswifi.model.response;

import com.maviance.middleware_enkap_expresswifi.enums.ExpressWifiStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private ExpressWifiStatus status;
}
