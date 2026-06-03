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

                redisService.incrementUserBalance(event.getUserId(), totalValue);
            }

            Trade saved = persistTrade(event, executionPrice, totalValue, TradeStatus.EXECUTED);

            log.info("EXECUTED. TradeId={} User={} Ticker={} Type={} Qty={} Price={} Total={}",
                    saved.getId(), event.getUserId(), event.getTicker(),
                    event.getType(), event.getQuantity(), executionPrice, totalValue);

        } catch (Exception e) {
            log.error("Failed to process event {}. Error: {}", event.getEventId(), e.getMessage(), e);
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