package com.tradeengine.trade_engine.cucumber;

import com.tradeengine.trade_engine.config.TestConfig;
import com.tradeengine.trade_engine.events.TradeRequestedEvent;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.batch.job.enabled=false"
})
public class CucumberSpringConfiguration {

    @MockitoBean
    KafkaTemplate<String, TradeRequestedEvent> kafkaTemplate;
}