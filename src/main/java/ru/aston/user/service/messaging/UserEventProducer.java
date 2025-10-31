package ru.aston.user.service.messaging;

import ru.aston.common.dto.UserEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${app.kafka.topic.user-events}")
    private String topic;

    public void sendEvent(UserEvent event) {
        kafkaTemplate.send(topic, event.getEmail(), event);
        log.info("\uD83D\uDCE4 Sent event to Kafka: {}", event);
    }
}
