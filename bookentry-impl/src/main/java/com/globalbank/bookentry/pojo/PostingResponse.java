package com.globalbank.bookentry.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostingResponse {
    public final String responseCode;
    public final String narrative;
    public final String creditDebitIndicator;
}
