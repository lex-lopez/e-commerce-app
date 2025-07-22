package com.alopez.store.payments.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckOutRequest {
    @NotNull(message = "Cart Id is required")
    private UUID cartId;
}
