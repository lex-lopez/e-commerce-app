package com.alopez.store.payments.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CheckOutResponse {
    private Long orderId;
    private String checkoutUrl;
}
