package com.board.wars.historize;

import com.board.wars.History;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Configuration
public class HistorizeConfig {

    private final KafkaProperties kafkaProperties;

    public HistorizeConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, Object> historizeConsumerFactory() {
        final JsonDeserializer<Object> deserializer = new JsonDeserializer<>();
        deserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), deserializer
        );
    }


    @Bean("historizeSink")
    public Sinks.Many<History>  historizeSink() {
        return sinkFactory();
    }

    private Sinks.Many<History>  sinkFactory() {
        return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }
}
