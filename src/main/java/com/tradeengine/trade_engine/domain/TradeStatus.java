package com.tradeengine.trade_engine.domain;

public enum TradeStatus {
    REQUESTED,  // accepted by the REST API, placed on the Kafka topic
    EXECUTED,   // read from Kafka, balance checked, saved to PostgreSQL
    SETTLED,    // processed by the overnight Spring Batch job
    REJECTED    // insufficient balance or validation failure
}