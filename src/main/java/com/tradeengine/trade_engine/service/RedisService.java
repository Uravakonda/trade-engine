package com.tradeengine.trade_engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String BALANCE_KEY_PREFIX = "balance:";
    private static final String PRICE_KEY_PREFIX = "price:";

    public void setUserBalance(String userId, BigDecimal balance) {
        redisTemplate.opsForValue().set(
                BALANCE_KEY_PREFIX + userId,
                balance.toPlainString()
        );
        log.debug("Set balance for user {}: {}", userId, balance);
    }

    public BigDecimal getUserBalance(String userId) {
        String value = redisTemplate.opsForValue().get(BALANCE_KEY_PREFIX + userId);
        if (value == null) {
            log.warn("No balance in Redis for user: {}. Creating default $10,000.", userId);
            BigDecimal defaultBalance = new BigDecimal("10000.00");
            setUserBalance(userId, defaultBalance);
            return defaultBalance;
        }
        return new BigDecimal(value);
    }

    public void decrementUserBalance(String userId, BigDecimal amount) {
        BigDecimal current = getUserBalance(userId);
        setUserBalance(userId, current.subtract(amount));
        log.debug("Decremented balance for {} by {}. New balance: {}",
                userId, amount, current.subtract(amount));
    }

    public void incrementUserBalance(String userId, BigDecimal amount) {
        BigDecimal current = getUserBalance(userId);
        setUserBalance(userId, current.add(amount));
        log.debug("Incremented balance for {} by {}. New balance: {}",
                userId, amount, current.add(amount));
    }


    public void setMarketPrice(String ticker, BigDecimal price) {
        redisTemplate.opsForValue().set(
                PRICE_KEY_PREFIX + ticker,
                price.toPlainString(),
                60, TimeUnit.SECONDS
        );
        log.debug("Set market price for {}: {}", ticker, price);
    }

    public BigDecimal getMarketPrice(String ticker) {
        String value = redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + ticker);
        if (value == null) {
            log.warn("No market price in Redis for {}. Using hardcoded default.", ticker);
            return getDefaultPrice(ticker);
        }
        return new BigDecimal(value);
    }

    private BigDecimal getDefaultPrice(String ticker) {
        return switch (ticker.toUpperCase()) {
            case "AAPL"  -> new BigDecimal("185.50");
            case "MSFT"  -> new BigDecimal("420.00");
            case "TSLA"  -> new BigDecimal("245.00");
            case "GOOGL" -> new BigDecimal("175.00");
            default      -> new BigDecimal("100.00");
        };
    }
}