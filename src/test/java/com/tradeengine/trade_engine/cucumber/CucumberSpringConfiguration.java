package com.tradeengine.trade_engine.cucumber;

import com.tradeengine.trade_engine.config.TestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.batch.job.enabled=false"
})
public class CucumberSpringConfiguration {
}