package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
public class UpdateAccountDto {
    public final String accountNumber;
    public final String accountName;
    public final String accountType;

    @JsonCreator
    public UpdateAccountDto(@JsonProperty(value = "accountNumber") String accountNumber,
                            @JsonProperty(value = "accountName") String accountName,
                            @JsonProperty(value = "accountType") String accountType) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountType = accountType;
    }
}
