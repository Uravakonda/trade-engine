package com.tradeengine.trade_engine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/*
 * Basic smoke test — verifies the Spring context loads without errors.
 *
 * spring.kafka.bootstrap-servers=localhost:9999 points at a non-existent
 * broker so Spring Kafka does not attempt a real connection during tests.
 * Spring Kafka's auto-configuration still creates the KafkaTemplate bean
 * (which is all we need to satisfy the TradeController dependency) but
 * does not block waiting for a live broker at startup.
 *
 * spring.batch.job.enabled=false prevents Spring Batch from automatically
 * running the settlement job when the context loads during tests.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.batch.job.enabled=false"
})
class TradeEngineApplicationTests {

    @Test
    void contextLoads() {
        // If this method runs without throwing, the Spring context
        // loaded successfully — all beans wired, all configs valid.
    }
}