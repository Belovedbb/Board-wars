package com.board.wars.historize.kanban.stub;

import com.board.wars.History;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class KanbanConsumer {

    private final Sinks.Many<History> kanbanSink;
    private final Sinks.Many<History> projectSink;
    private final Sinks.Many<History> graphicsSink;
    private final Sinks.Many<History> historizeSink;

    public KanbanConsumer(Sinks.Many<History> kanbanSink, Sinks.Many<History> projectSink, Sinks.Many<History> graphicsSink,
                          Sinks.Many<History> historizeSink) {
        this.kanbanSink = kanbanSink;
        this.projectSink = projectSink;
        this.graphicsSink = graphicsSink;
        this.historizeSink = historizeSink;
    }

    @KafkaListener(topics = "kanban",  groupId = "delegate", containerFactory = "kanbanConsumerGroup")
    public void groupAll(ConsumerRecord<String, History> cr, @Payload History payload) {
        //logger.info("Logger 1 [JSON] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(), payload, cr.toString());
        this.kanbanSink.tryEmitNext(payload).orThrow();
        this.historizeSink.tryEmitNext(payload).orThrow();
    }

    @KafkaListener(topics = "kanban", clientIdPrefix = "kanban", groupId = "delegate", containerFactory = "projectConsumerGroup",
            topicPartitions = {@TopicPartition(topic = "kanban", partitions = "0" )}
            )
    public void kafka1ListenerContainerFactory(ConsumerRecord<String, History> cr, @Payload History payload) {
        //logger.info("Logger 1 [JSON] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),payload, cr.toString());
        this.projectSink.tryEmitNext(payload).orThrow();
        this.historizeSink.tryEmitNext(payload).orThrow();
    }

    @KafkaListener(topics = "kanban", clientIdPrefix = "kanban",  groupId = "delegate", containerFactory = "graphicsConsumerGroup",
            topicPartitions = {
                    @TopicPartition(topic = "kanban", partitions = "1")
            })
    public void kafka2ListenerContainerFactory(ConsumerRecord<String, History> cr, @Payload History payload) {
        //logger.info("Logger 1 [JSON] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),  payload, cr.toString());
        this.graphicsSink.tryEmitNext(payload).orThrow();
        this.historizeSink.tryEmitNext(payload).orThrow();
    }

}
