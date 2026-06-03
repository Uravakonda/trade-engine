package com.tradeengine.trade_engine.events;

import com.tradeengine.trade_engine.domain.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequestedEvent {
    private String eventId;
    private String userId;
    private String ticker;
    private TradeType type;
    private BigDecimal quantity;
    private BigDecimal requestedPrice;
    private long timestamp;
}