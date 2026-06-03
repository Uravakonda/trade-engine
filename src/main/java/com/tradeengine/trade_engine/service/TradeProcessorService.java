package com.tradeengine.trade_engine.service;

import com.tradeengine.trade_engine.config.KafkaConfig;
import com.tradeengine.trade_engine.domain.Trade;
import com.tradeengine.trade_engine.domain.TradeStatus;
import com.tradeengine.trade_engine.events.TradeRequestedEvent;
import com.tradeengine.trade_engine.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeProcessorService {

    private final TradeRepository tradeRepository;
    private final RedisService redisService;

    /*
     * @KafkaListener subscribes this method to the trade-requests topic.
     * Spring Kafka manages the entire consumer lifecycle: connecting to
     * the broker, polling for messages, deserialising the JSON bytes back
     * into a TradeRequestedEvent object, and calling this method.
     *
     * The @Payload annotation extracts the deserialised message body.
     * The @Header annotations extract Kafka metadata — the partition number
     * and offset are logged so you can trace any message through the system.
     *
     * @Transactional wraps the database save in a transaction. If the
     * save throws an exception, the transaction rolls back. Note that
     * the Redis balance decrement is NOT rolled back if the DB save fails —
     * Redis has no transaction coordination with PostgreSQL. In production
     * systems this is handled with a distributed saga pattern or by using
     * Redis as the authoritative source and reconciling later.
     */
    @KafkaListener(
            topics = KafkaConfig.TRADE_REQUESTS_TOPIC,
            groupId = "trade-processor-group",
            concurrency = "3"
    )
    @Transactional
    public void processTrade(
            @Payload TradeRequestedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Processing event. EventId={} Partition={} Offset={}",
                event.getEventId(), partition, offset);

        try {
            BigDecimal executionPrice = redisService.getMarketPrice(event.getTicker());
            BigDecimal totalValue = event.getQuantity().multiply(executionPrice);
            BigDecimal userBalance = redisService.getUserBalance(event.getUserId());

            if (event.getType().name().equals("BUY")) {
                if (userBalance.compareTo(totalValue) < 0) {
                    log.warn("REJECTED. User={} Required={} Available={}",
                            event.getUserId(), totalValue, userBalance);
                    persistTrade(event, executionPrice, totalValue, TradeStatus.REJECTED);
                    return;
                }
                redisService.decrementUserBalance(event.getUserId(), totalValue);
            } else {
                // SELL — credit the proceeds back to the user's balance
                redisService.incrementUserBalance(event.getUserId(), totalValue);
            }

            Trade saved = persistTrade(event, executionPrice, totalValue, TradeStatus.EXECUTED);

            log.info("EXECUTED. TradeId={} User={} Ticker={} Type={} Qty={} Price={} Total={}",
                    saved.getId(), event.getUserId(), event.getTicker(),
                    event.getType(), event.getQuantity(), executionPrice, totalValue);

        } catch (Exception e) {
            log.error("Failed to process event {}. Error: {}", event.getEventId(), e.getMessage(), e);
            // In production: configure @RetryableTopic or DeadLetterPublishingRecoverer
            // so failed messages are retried then sent to a dead-letter topic
            // for manual review, rather than being silently dropped.
        }
    }

    private Trade persistTrade(TradeRequestedEvent event, BigDecimal price,
                               BigDecimal totalValue, TradeStatus status) {
        Trade trade = Trade.builder()
                .userId(event.getUserId())
                .ticker(event.getTicker())
                .type(event.getType())
                .quantity(event.getQuantity())
                .price(price)
                .totalValue(totalValue)
                .status(status)
                .processedAt(LocalDateTime.now())
                .build();
        return tradeRepository.save(trade);
    }
}