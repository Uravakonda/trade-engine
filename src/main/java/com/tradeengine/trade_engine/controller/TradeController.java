package com.tradeengine.trade_engine.controller;

import com.tradeengine.trade_engine.config.KafkaConfig;
import com.tradeengine.trade_engine.domain.TradeStatus;
import com.tradeengine.trade_engine.dto.TradeRequest;
import com.tradeengine.trade_engine.dto.TradeResponse;
import com.tradeengine.trade_engine.events.TradeRequestedEvent;
import com.tradeengine.trade_engine.service.RedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TradeController {

    private final KafkaTemplate<String, TradeRequestedEvent> kafkaTemplate;
    private final RedisService redisService;

    /*
     * POST /api/v1/trades
     *
     * @Valid on the parameter triggers Bean Validation on the request body.
     * If any @NotBlank / @NotNull / @DecimalMin constraint fails,
     * Spring returns 400 Bad Request automatically — this method is
     * never even called in that case.
     *
     * The controller is intentionally thin: validate → build event →
     * publish to Kafka → return 202. No business logic lives here.
     * Business logic lives in TradeProcessorService.
     */
    @PostMapping("/trades")
    public ResponseEntity<TradeResponse> submitTrade(
            @Valid @RequestBody TradeRequest request) {

        String eventId = UUID.randomUUID().toString();

        log.info("Trade received. EventId={} User={} Ticker={} Type={} Qty={}",
                eventId, request.getUserId(), request.getTicker(),
                request.getType(), request.getQuantity());

        TradeRequestedEvent event = TradeRequestedEvent.builder()
                .eventId(eventId)
                .userId(request.getUserId())
                .ticker(request.getTicker())
                .type(request.getType())
                .quantity(request.getQuantity())
                .requestedPrice(request.getPrice())
                .timestamp(System.currentTimeMillis())
                .build();

        // The userId is the Kafka message key. Using the same key for
        // all messages from the same user means they are routed to the
        // same partition, guaranteeing processing order per user.
        kafkaTemplate.send(KafkaConfig.TRADE_REQUESTS_TOPIC, request.getUserId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Published to Kafka. Partition={} Offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Kafka publish failed for eventId={}: {}", eventId, ex.getMessage());
                    }
                });

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TradeResponse.builder()
                        .eventId(eventId)
                        .userId(request.getUserId())
                        .ticker(request.getTicker())
                        .type(request.getType().name())
                        .quantity(request.getQuantity())
                        .price(request.getPrice())
                        .status(TradeStatus.REQUESTED)
                        .message("Trade accepted and queued for processing")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Trade Engine running");
    }

    // Utility endpoints for seeding test data — not part of a real production API
    @PostMapping("/market/prices/{ticker}/{price}")
    public ResponseEntity<String> setMarketPrice(
            @PathVariable String ticker,
            @PathVariable BigDecimal price) {
        redisService.setMarketPrice(ticker, price);
        return ResponseEntity.ok("Price set: " + ticker + " = " + price);
    }

    @PostMapping("/users/{userId}/balance/{amount}")
    public ResponseEntity<String> setUserBalance(
            @PathVariable String userId,
            @PathVariable BigDecimal amount) {
        redisService.setUserBalance(userId, amount);
        return ResponseEntity.ok("Balance set: " + userId + " = " + amount);
    }
}