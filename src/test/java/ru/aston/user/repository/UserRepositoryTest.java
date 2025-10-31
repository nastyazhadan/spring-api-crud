package ru.aston.user.repository;

import ru.aston.common.dto.UserEvent;
import ru.aston.user.UserServiceAppTest;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    MockMvc mockMvc;

    @TestConfiguration
    static class KafkaMockConfig {
        @Bean
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, UserEvent> kafkaTemplate() {
            return (KafkaTemplate<String, UserEvent>) mock(KafkaTemplate.class);
        }
    }

    @Test
    void shouldReturn500WhenDuplicateEmail() throws Exception {
        String json1 = UserServiceAppTest.createUserJson("Lena", "duplicate@mail.ru", 25);
        String json2 = UserServiceAppTest.createUserJson("Other", "duplicate@mail.ru", 30);

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(json1))
                        .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(json2))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message")
                            .value("User with this email duplicate@mail.ru already exists"));
    }

    @Test
    void shouldReturn500WhenDatabaseIsDownOnCreate() throws Exception {
        postgres.stop();

        String json = UserServiceAppTest.createUserJson("FailCreate", "failCreate@gmail.com", 40);

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                        .andExpect(jsonPath("$.message")
                            .value("A server error occurred. Please try again later."));
    }
}
