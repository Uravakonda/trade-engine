package com.tradeengine.trade_engine.domain;

public enum TradeStatus {
    REQUESTED,  //accepted by REST API and placed on Kafka topic
    EXECUTED,   //read from Kafka, balance checked, saved to PostgreSQL
    SETTLED,    //processed by Spring Batch job
    REJECTED    //insufficient balance or validation issue
}