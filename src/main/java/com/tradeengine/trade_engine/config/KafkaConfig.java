package com.tradeengine.trade_engine.config;

import com.tradeengine.trade_engine.events.TradeRequestedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

// Spring Kafka 4.0: JacksonJsonSerializer replaces the deprecated JsonSerializer.
// It is in the same package but built on Jackson 3 (tools.jackson) instead of
// the old Jackson 2 (com.fasterxml.jackson).
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TRADE_REQUESTS_TOPIC = "trade-requests";
    public static final String TRADE_EXECUTED_TOPIC = "trade-executed";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /*
     * Theory — why pass serializers as constructor arguments rather than config map entries:
     *
     * The old pattern was:
     *   config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class)
     * This put a Class reference in the map, and Kafka instantiated it reflectively.
     *
     * The Spring Kafka 4.0 pattern is to pass serializer INSTANCES directly to
     * DefaultKafkaProducerFactory's constructor. This is more type-safe, avoids
     * reflection, and allows the serializer to be configured programmatically
     * before being handed to the factory.
     *
     * The three-argument constructor is:
     *   DefaultKafkaProducerFactory(Map<String,Object> configs,
     *                               Serializer<K> keySerializer,
     *                               Serializer<V> valueSerializer)
     */
    @Bean
    public ProducerFactory<String, TradeRequestedEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // MAX_BLOCK_MS: how long the producer will block waiting for metadata
        // or buffer space. 5 seconds prevents the app hanging indefinitely
        // if Kafka is temporarily unreachable at startup.
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new JacksonJsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, TradeRequestedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic tradeRequestsTopic() {
        return TopicBuilder.name(TRADE_REQUESTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic tradeExecutedTopic() {
        return TopicBuilder.name(TRADE_EXECUTED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}