package com.globalbank.bookentry.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class PostingRequest {
    public final String reference;
    public final String sourceAccount;
    public final String destinationAccount;
    public final String narrative;
    public final String currencyCode;
    public final BigDecimal amount;


}
