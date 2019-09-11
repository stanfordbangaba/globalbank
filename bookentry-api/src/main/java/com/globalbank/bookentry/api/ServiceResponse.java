package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

@Value
public class ServiceResponse {
    public final String responseCode;
    public final String narrative;

    @JsonCreator
    public ServiceResponse(@JsonProperty(value = "responseCode") String responseCode,
                           @JsonProperty(value = "narrative") String narrative) {
        this.responseCode = responseCode;
        this.narrative = narrative;
    }

    public static ServiceResponse of(String responseCode, String narrative) {
        return new ServiceResponse(responseCode, narrative);
    }
}
