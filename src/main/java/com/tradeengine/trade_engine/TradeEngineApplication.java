package com.tradeengine.trade_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradeEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeEngineApplication.class, args);
    }
}