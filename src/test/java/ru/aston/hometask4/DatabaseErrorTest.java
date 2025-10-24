package ru.aston.hometask4;

import ru.aston.hometask4.entity.User;
import ru.aston.hometask4.repository.UserRepository;
import static ru.aston.hometask4.UserServiceAppTest.createUserJson;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import java.util.Optional;


@SpringBootTest
@AutoConfigureMockMvc
class DatabaseErrorTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldHandleDatabaseErrorOnCreate() throws Exception {
        doThrow(new DataAccessResourceFailureException("Simulated DB failure")).when(userRepository).save(any());

        String json = createUserJson("FailCreate", "failCreate@gmail.com", 40);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                        .andExpect(jsonPath("$.message").
                                value("A server error occurred. Please try again later."));
    }

    @Test
    void shouldHandleDatabaseErrorOnUpdate() throws Exception {
        String json = createUserJson("FailUpdate", "failUpdate@mail.ru", 35);

        when(userRepository.existsById(1)).thenReturn(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));

        doThrow(new DataAccessException("Simulated DB failure") {}).when(userRepository).save(any());

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                        .andExpect(jsonPath("$.message")
                        .value("A server error occurred. Please try again later."));
    }

    @Test
    void shouldHandleDatabaseErrorOnDelete() throws Exception {
        User user = new User();
        user.setId(1);

        when(userRepository.existsById(1)).thenReturn(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        doThrow(new DataAccessException("Simulated DB failure") {}).when(userRepository).delete(any(User.class));

        mockMvc.perform(delete("/users/1"))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                        .andExpect(jsonPath("$.message")
                                .value("A server error occurred. Please try again later."));
    }
}
