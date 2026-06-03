package com.tradeengine.trade_engine.events;

import com.tradeengine.trade_engine.domain.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/*
 * Theory — Events vs Entities:
 *
 * This class is NOT a JPA entity and has no @Entity annotation.
 * It is a plain Java object (POJO) that gets serialised to JSON
 * and sent through Kafka.
 *
 * The naming convention "TradeRequestedEvent" follows event-driven
 * architecture conventions: events are named in the past tense
 * ("something happened") and represent facts, not commands.
 * "TradeRequestedEvent" means "the fact that a trade was requested".
 *
 * @NoArgsConstructor is required by Jackson's JSON deserialiser —
 * it needs to create an empty object first, then populate the fields.
 */
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