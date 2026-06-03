package com.tradeengine.trade_engine.dto;

import com.tradeengine.trade_engine.domain.TradeType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;


@Data
public class TradeRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Ticker symbol is required")
    @Size(min = 1, max = 5, message = "Ticker must be 1-5 characters")
    private String ticker;

    @NotNull(message = "Trade type is required (BUY or SELL)")
    private TradeType type;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;
}