package com.tradeengine.trade_engine;

import com.tradeengine.trade_engine.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.batch.job.enabled=false"
})
class TradeEngineApplicationTests {

    @Test
    void contextLoads() {
    }
}