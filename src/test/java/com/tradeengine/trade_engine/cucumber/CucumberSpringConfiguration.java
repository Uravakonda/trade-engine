package com.tradeengine.trade_engine.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Configuration  // Required: without this, @Bean methods are invisible to Spring Boot 4.x
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.batch.job.enabled=false"
})
public class CucumberSpringConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}