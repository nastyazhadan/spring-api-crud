package ru.aston.user.service.messaging;

import ru.aston.common.dto.UserEvent;
import ru.aston.common.dto.UserEventType;
import ru.aston.user.entity.User;
import ru.aston.user.service.core.UserService;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "user.events")
class UserServiceKafkaIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private ConsumerFactory<String, UserEvent> consumerFactory;

    private Consumer<String, UserEvent> createConsumer() {
        Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.aston.common.dto.UserEvent");

        Consumer<String, UserEvent> consumer =
                new DefaultKafkaConsumerFactory<String, UserEvent>(props)
                        .createConsumer("test-group", "test-client");
        consumer.subscribe(List.of("user.events"));
        return consumer;
    }

    @Test
    void shouldPublishEventWhenUserCreated() {
        Consumer<String, UserEvent> consumer = createConsumer();

        userService.createUser(new User("Test", "test@mail.com", 25));

        ConsumerRecords<String, UserEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertFalse(records.isEmpty(), "No Kafka messages received!");

        UserEvent event = records.iterator().next().value();
        assertEquals(UserEventType.CREATED, event.getUserEventType());
        assertEquals("test@mail.com", event.getEmail());

        consumer.close();
    }

    @Test
    void shouldPublishEventWhenUserDeleted() {
        Consumer<String, UserEvent> consumer = createConsumer();

        User createdUser = userService.createUser(new User("DeleteMe", "deleteme@mail.com", 28));
        userService.deleteUser(createdUser.getId());

        ConsumerRecords<String, UserEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertFalse(records.isEmpty(), "No Kafka messages received on delete!");

        List<UserEvent> events = new ArrayList<>();
        records.records("user.events").forEach(r -> events.add(r.value()));
        UserEvent lastEvent = events.get(events.size() - 1);

        assertEquals(UserEventType.DELETED, lastEvent.getUserEventType());
        assertEquals("deleteme@mail.com", lastEvent.getEmail());

        consumer.close();
    }
}
