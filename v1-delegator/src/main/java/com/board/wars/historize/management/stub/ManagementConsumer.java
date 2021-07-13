package com.board.wars.historize.management.stub;

import com.board.wars.History;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class ManagementConsumer {

    private final Sinks.Many<History> managementSink;
    private final Sinks.Many<History> historizeSink;

    public ManagementConsumer(Sinks.Many<History> managementSink, Sinks.Many<History> historizeSink) {
        this.managementSink = managementSink;
        this.historizeSink = historizeSink;
    }

    @KafkaListener(topics = "management",  groupId = "delegate", containerFactory = "managementConsumerGroup")
    public void groupAll(ConsumerRecord<String, History> cr, @Payload History payload) {
        //logger.info("Logger 1 [JSON] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(), payload, cr.toString());
        this.managementSink.tryEmitNext(payload).orThrow();
        this.historizeSink.tryEmitNext(payload).orThrow();
    }

}
