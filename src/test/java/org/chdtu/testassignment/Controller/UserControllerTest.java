package org.chdtu.testassignment.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chdtu.testassignment.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;



    @Autowired
    private ObjectMapper objectMapper;

    Map<String, User> userStore = new HashMap<>();
    User userDefault;

    @BeforeEach
    void setUp() {
        userDefault = new User(
                "test@gmail.com",
                "DAD",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                "1234567890");
        userStore.clear();
        userStore.put(userDefault.getEmail(), userDefault);
    }

    @Test
    void createUserSuccess() throws Exception {
        ResultActions result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDefault)));
        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.email").value(userDefault.getEmail()));
    }

    @Test
    void createUserInvalid() throws Exception {
        User underageUser = new User(
                "underage@example.com",
                "John",
                "Doe",
                LocalDate.now().plusDays(10),
                "123 Main St",
                "1234567890"
        );
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(underageUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserPartialSuccess() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDefault)))
                .andExpect(status().isCreated());

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", "Jane");
        updates.put("lastName", "Smith");
        updates.put("address", "Che ...");
        updates.put("phoneNumber", "123456789");
        updates.put("birthDate", LocalDate.of(1993, 1, 1));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate expectedBirthDate = (LocalDate) updates.get("birthDate");
        String expectedBirthDateString = expectedBirthDate.format(formatter);

        ResultActions result = mockMvc.perform(patch("/users/{email}", userDefault.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.email").value(userDefault.getEmail()));
        result.andExpect(jsonPath("$.firstName").value(updates.get("firstName")));
        result.andExpect(jsonPath("$.lastName").value(updates.get("lastName")));
        result.andExpect(jsonPath("$.address").value(updates.get("address")));
        result.andExpect(jsonPath("$.phoneNumber").value(updates.get("phoneNumber")));
        result.andExpect(jsonPath("$.birthDate").value(expectedBirthDateString));
    }

    @Test
    void updateUserPartialInvalid() throws Exception {
        LocalDate invalidBirthDate = LocalDate.now().plusYears(10);
        String invalidBirthDateString = invalidBirthDate.toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", "Jane");
        updates.put("lastName", "Smith");
        updates.put("birthDate", invalidBirthDateString);

        ResultActions result = mockMvc.perform(patch("/users/{email}", "tmp_" + userDefault.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)));
        result.andExpect(status().isNotFound());
    }

    @Test
    void updateUserFullSuccess() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDefault)))
                .andExpect(status().isCreated());

        User updateUser = new User(
                "new_test@gmail.com",
                "And",
                "Klm",
                LocalDate.of(1987, 2, 3),
                "22 st",
                "1987654321");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        ResultActions result = mockMvc.perform(put("/users/{email}", userDefault.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.email").value(updateUser.getEmail()));
        result.andExpect(jsonPath("$.firstName").value(updateUser.getFirstName()));
        result.andExpect(jsonPath("$.lastName").value(updateUser.getLastName()));
        result.andExpect(jsonPath("$.birthDate").value(updateUser.getBirthDate().format(formatter)));
    }

    @Test
    void updateUserFullInvalid() throws Exception {
        User updateUser = new User(
                "new_test@gmail.com",
                "And",
                "Klm",
                LocalDate.of(1987, 2, 3),
                "22 st",
                "1987654321");

        mockMvc.perform(put("/users/{email}", "tmp_" + updateUser.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserSuccess() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDefault)))
                .andExpect(status().isCreated());

        ResultActions result = mockMvc.perform(delete("/users/{email}", userDefault.getEmail())
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNoContent());
    }

    @Test
    void deleteUserInvalid() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{email}", "tmp_" + userDefault.getEmail())
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    void searchUsersByBirthDateSuccess() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDefault)))
                .andExpect(status().isCreated());

        LocalDate from = LocalDate.of(1987, 2, 3);
        LocalDate to = LocalDate.of(1995, 3, 13);
        ResultActions result = mockMvc.perform(get("/users/search")
                .param("from", from.toString())
                .param("to", to.toString())
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$[0].email").value(userDefault.getEmail()));
    }

    @Test
    void searchUsersByBirthDateInvalid() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDefault)))
                .andExpect(status().isCreated());

        LocalDate from = LocalDate.of(1997, 2, 3);
        LocalDate to = LocalDate.of(1985, 3, 13);
        ResultActions result = mockMvc.perform(get("/users/search")
                .param("from", from.toString())
                .param("to", to.toString())
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isBadRequest());
    }
}