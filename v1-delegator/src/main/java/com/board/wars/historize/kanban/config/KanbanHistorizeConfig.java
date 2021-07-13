package com.board.wars.historize.kanban.config;

import com.board.wars.History;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Configuration
public class KanbanHistorizeConfig {

    final private ConsumerFactory<String, Object> historizeConsumerFactory;

    public KanbanHistorizeConfig(ConsumerFactory<String, Object> historizeConsumerFactory) {
        this.historizeConsumerFactory = historizeConsumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kanbanConsumerGroup() {
        return groupFactory();
    }

    @Bean("kanbanSink")
    public Sinks.Many<History>  kanbanSink() {
        return sinkFactory();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> projectConsumerGroup() {
        return groupFactory();
    }

    @Bean("projectSink")
    public Sinks.Many<History>  projectSink() {
        return sinkFactory();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> graphicsConsumerGroup() {
        return groupFactory();
    }

    @Bean("graphicsSink")
    public Sinks.Many<History>  graphicsSink() {
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
