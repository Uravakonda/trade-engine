package com.tradeengine.trade_engine.dto;

import com.tradeengine.trade_engine.domain.TradeStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TradeResponse {
    private String eventId;
    private String userId;
    private String ticker;
    private String type;
    private BigDecimal quantity;
    private BigDecimal price;
    private TradeStatus status;
    private String message;
    private LocalDateTime timestamp;
}