package com.tradeengine.trade_engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeengine.trade_engine.domain.TradeType;
import com.tradeengine.trade_engine.dto.TradeRequest;
import com.tradeengine.trade_engine.events.TradeRequestedEvent;
import com.tradeengine.trade_engine.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * @WebMvcTest loads only the web layer — no database, no Kafka, no Redis.
 * Spring Boot 4 no longer exposes ObjectMapper as a bean in the WebMvcTest
 * slice context (it now uses Jackson 3's JsonMapper internally).
 * We create ObjectMapper directly — it does not need to be a Spring bean
 * for JSON serialisation in tests.
 */
@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Instantiate directly — NOT @Autowired — because ObjectMapper is not
    // a registered bean in the Spring Boot 4 WebMvcTest slice context.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private KafkaTemplate<String, TradeRequestedEvent> kafkaTemplate;

    @MockitoBean
    private RedisService redisService;

    @Test
    @DisplayName("Valid BUY order returns 202 Accepted with REQUESTED status")
    void submitTrade_validBuy_returns202() throws Exception {
        TradeRequest request = buildRequest("user1", "AAPL", TradeType.BUY, "185.50", "10");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.eventId").exists());
    }

    @Test
    @DisplayName("Valid SELL order returns 202 Accepted")
    void submitTrade_validSell_returns202() throws Exception {
        TradeRequest request = buildRequest("user2", "TSLA", TradeType.SELL, "245.00", "5");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("Missing userId returns 400 Bad Request")
    void submitTrade_missingUserId_returns400() throws Exception {
        TradeRequest request = buildRequest(null, "AAPL", TradeType.BUY, "185.50", "10");

        mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Negative quantity returns 400 Bad Request")
    void submitTrade_negativeQuantity_returns400() throws Exception {
        TradeRequest request = buildRequest("user1", "AAPL", TradeType.BUY, "185.50", "-5");

        mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private TradeRequest buildRequest(String userId, String ticker,
                                      TradeType type, String price, String quantity) {
        TradeRequest req = new TradeRequest();
        req.setUserId(userId);
        req.setTicker(ticker);
        req.setType(type);
        req.setPrice(price != null ? new BigDecimal(price) : null);
        req.setQuantity(quantity != null ? new BigDecimal(quantity) : null);
        return req;
    }
}