package com.maviance.middleware_enkap_expresswifi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ErrorResponse extends CustomResponse {
    private ErrorObject error;
    public ErrorResponse(ErrorObject error) {
        this.error = error;
    }


    @Data
    @With
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorObject{
        private String message;
        private String type;
        private int code;
        @JsonProperty("fbtrace_id")
        private String fbTraceID;
    }
}


