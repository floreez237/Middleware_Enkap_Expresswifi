package com.maviance.middleware_enkap_expresswifi.model.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ENkapStatusResponse {
    private ENkapStatus status;

    public static enum ENkapStatus {
        CONFIRMED, IN_PROGRESS, FAILED, CANCELED, CREATED, INITIALISED
    }

}
