package com.tradeengine.trade_engine.dto;

import com.tradeengine.trade_engine.domain.TradeType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/*
 * Theory — DTOs (Data Transfer Objects):
 *
 * A DTO is the shape of data crossing a boundary — in this case,
 * the JSON body a client sends to the REST API.
 *
 * Never expose your JPA entity directly as an API request/response.
 * If you did, a client could try to set the 'id' or 'status' fields
 * directly, bypassing your business logic. DTOs give you full control
 * over what the outside world can send in.
 *
 * Bean Validation annotations (@NotBlank, @NotNull, @DecimalMin) are
 * processed by Spring when you put @Valid on the controller method
 * parameter. If any constraint fails, Spring automatically returns
 * HTTP 400 Bad Request with details of which fields failed — you write
 * zero validation code yourself.
 */
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