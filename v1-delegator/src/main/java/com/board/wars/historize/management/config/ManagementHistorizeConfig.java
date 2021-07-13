package com.board.wars.historize.management.config;

import com.board.wars.History;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Configuration
public class ManagementHistorizeConfig {

    final private ConsumerFactory<String, Object> historizeConsumerFactory;

    public ManagementHistorizeConfig(ConsumerFactory<String, Object> historizeConsumerFactory) {
        this.historizeConsumerFactory = historizeConsumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> managementConsumerGroup() {
        return groupFactory();
    }

    @Bean("managementSink")
    public Sinks.Many<History>  managementSink() {
        return sinkFactory();
    }

    private ConcurrentKafkaListenerContainerFactory<String, Object> groupFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(historizeConsumerFactory);
        return factory;
    }

    private Sinks.Many<History>  sinkFactory() {
        return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

}
