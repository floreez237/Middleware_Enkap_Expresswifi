package com.maviance.middleware_enkap_expresswifi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CallBackObject {
    private String callbackUrl;
    private long timestamp;
}
