package com.tradeengine.trade_engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeengine.trade_engine.events.TradeRequestedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;


@TestConfiguration
public class TestConfig {


    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    @Bean
    public KafkaTemplateMockConfigurer kafkaTemplateMockConfigurer(
            KafkaTemplate<String, TradeRequestedEvent> kafkaTemplate) {
        return new KafkaTemplateMockConfigurer(kafkaTemplate);
    }


    public static class KafkaTemplateMockConfigurer {

        public KafkaTemplateMockConfigurer(
                KafkaTemplate<String, TradeRequestedEvent> kafkaTemplate) {

            RecordMetadata metadata = new RecordMetadata(
                    new TopicPartition("trade-requests", 0),
                    0L, 0, 0L, 0, 0);

            ProducerRecord<String, TradeRequestedEvent> record =
                    new ProducerRecord<>("trade-requests", "test-key", null);

            SendResult<String, TradeRequestedEvent> sendResult =
                    new SendResult<>(record, metadata);

            CompletableFuture<SendResult<String, TradeRequestedEvent>> future =
                    CompletableFuture.completedFuture(sendResult);


            Mockito.when(kafkaTemplate.send(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any()
            )).thenReturn(future);
        }
    }
}