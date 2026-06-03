package com.tradeengine.trade_engine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Returns balance from Redis when key exists")
    void getUserBalance_whenBalanceExists_returnsCorrectBalance() {
        when(valueOperations.get("balance:user123")).thenReturn("5000.00");

        BigDecimal result = redisService.getUserBalance("user123");

        assertThat(result).isEqualByComparingTo(new BigDecimal("5000.00"));
        verify(valueOperations).get("balance:user123");
    }

    @Test
    @DisplayName("Returns default 10000 when no balance key in Redis")
    void getUserBalance_whenNoBalance_returnsDefault() {
        when(valueOperations.get("balance:newUser")).thenReturn(null);

        BigDecimal result = redisService.getUserBalance("newUser");

        assertThat(result).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Decrement writes correct reduced balance back to Redis")
    void decrementUserBalance_writesCorrectNewBalance() {
        when(valueOperations.get("balance:user1")).thenReturn("1000.00");

        redisService.decrementUserBalance("user1", new BigDecimal("250.00"));

        verify(valueOperations).set("balance:user1", "750.00");
    }

    @Test
    @DisplayName("Returns hardcoded AAPL default when not in Redis")
    void getMarketPrice_forAAPL_returnsDefault() {
        when(valueOperations.get("price:AAPL")).thenReturn(null);

        BigDecimal price = redisService.getMarketPrice("AAPL");

        assertThat(price).isEqualByComparingTo(new BigDecimal("185.50"));
    }
}