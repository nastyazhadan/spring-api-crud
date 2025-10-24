package ru.aston.hometask4;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UserServiceAppTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAllUsers() throws Exception {
        String firstJson = createUserJson("Inna", "inna@mail.ru", 28);
        String secondJson = createUserJson("Anna", "anna@mail.ru", 31);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstJson))
                        .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondJson))
                        .andExpect(status().isCreated());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[1].email").isNotEmpty());
    }

	@Test
	void shouldCreateUser() throws Exception {
        String json = createUserJson("Nastya", "nastya@mail.ru", 30);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.name").value("Nastya"))
                        .andExpect(jsonPath("$.email").value("nastya@mail.ru"))
                        .andExpect(jsonPath("$.age").value(30));
	}

    @Test
    void shouldRejectInvalidUser() throws Exception {
        String json = createUserJson("Katya", "katya@gmail.com", -5);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").
                                value("age - Age should be more than 0"))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.status").
                                value(Matchers.containsString("BAD_REQUEST")));
    }

    @Test
    void shouldGetUserById() throws Exception {
        String json = createUserJson("Lena", "lena@mail.ru", 25);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        int id = node.get("id").asInt();

        mockMvc.perform(get("/users/" + id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("Lena"))
                        .andExpect(jsonPath("$.email").value("lena@mail.ru"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        String jsonCreate = createUserJson("Katya", "katya@mail.ru", 20);

        String created = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCreate))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        int id = mapper.readTree(created).get("id").asInt();

        String jsonUpdate = createUserJson("Katya", "katya@mail.com", 21);

        mockMvc.perform(patch("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                        .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidUpdatedUser() throws Exception {
        String jsonCreate = createUserJson("Kirill", "kirill@gmail.com", 20);

        String created = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCreate))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        int id = mapper.readTree(created).get("id").asInt();

        String jsonUpdate = createUserJson("Kirill", "kirill@gmail.com", -21);

        mockMvc.perform(patch("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").
                                value("age - Age should be more than 0"))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.status").
                                value(Matchers.containsString("BAD_REQUEST")));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        String json = createUserJson("Alex", "alex@mail.com", 32);

        String created = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        int id = mapper.readTree(created).get("id").asInt();

        mockMvc.perform(delete("/users/" + id))
                        .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundForNonExistingUser() throws Exception {
        mockMvc.perform(get("/users/9999"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").
                                value("User with ID 9999 not found"))
                        .andExpect(jsonPath("$.status").
                                value(Matchers.containsString("NOT_FOUND")));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingUser() throws Exception {
        mockMvc.perform(delete("/users/9999"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").
                                value("User with ID 9999 not found"))
                        .andExpect(jsonPath("$.status").
                                value(Matchers.containsString("NOT_FOUND")));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingUser() throws Exception {
        String json = createUserJson("Somename", "somename@gmail.com", 30);

        mockMvc.perform(patch("/users/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").
                                value("User with ID 9999 not found"))
                        .andExpect(jsonPath("$.status").
                                value(Matchers.containsString("NOT_FOUND")));
    }

    @Test
    void shouldRejectExistingUser() throws Exception {
        String json = createUserJson("Name", "email@mail.ru", 30);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isCreated());

        String existingJson = createUserJson("Name", "email@mail.ru", 29);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existingJson))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").
                                value("User with this email email@mail.ru already exists"))
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.status").
                                value(Matchers.containsString("BAD_REQUEST")));
    }

    public static String createUserJson(String name, String email, int age) {
        return String.format("""
        { "name": "%s", "email": "%s", "age": %d }
        """, name, email, age);
    }
}
