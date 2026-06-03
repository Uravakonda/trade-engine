package com.tradeengine.trade_engine;

import com.tradeengine.trade_engine.events.TradeRequestedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@MockitoBean(types = KafkaTemplate.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.batch.job.enabled=false"
})
class TradeEngineApplicationTests {

    @Test
    void contextLoads() {
    }
}