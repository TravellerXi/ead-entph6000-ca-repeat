package ie.tudublin.ead.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository repository;

    private ObjectNode registration(String username, String email, String password) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("email", email);
        node.put("password", password);
        return node;
    }

    @Test
    void registersUserAndNeverReturnsCredentials() throws Exception {
        User saved = new User("alice", "alice@tudublin.ie", "Alice");
        saved.setId("u1");
        saved.setPasswordHash("$2a$10$fakehashvalue");

        given(repository.existsByUsernameIgnoreCase("alice")).willReturn(false);
        given(repository.existsByEmailIgnoreCase("alice@tudublin.ie")).willReturn(false);
        given(repository.save(any(User.class))).willReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registration("alice", "alice@tudublin.ie", "sup3rsecret"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void rejectsShortPassword() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registration("bob", "bob@tudublin.ie", "short"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registration("carol", "not-an-email", "sup3rsecret"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateUsername() throws Exception {
        given(repository.existsByUsernameIgnoreCase("alice")).willReturn(true);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registration("alice", "other@tudublin.ie", "sup3rsecret"))))
                .andExpect(status().isConflict());
    }

    @Test
    void loginSucceedsWithCorrectPassword() throws Exception {
        User stored = new User("alice", "alice@tudublin.ie", "Alice");
        stored.setId("u1");
        stored.setPasswordHash(new BCryptPasswordEncoder().encode("sup3rsecret"));

        given(repository.findByUsernameIgnoreCase("alice")).willReturn(Optional.of(stored));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"sup3rsecret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        User stored = new User("alice", "alice@tudublin.ie", "Alice");
        stored.setPasswordHash(new BCryptPasswordEncoder().encode("sup3rsecret"));

        given(repository.findByUsernameIgnoreCase("alice")).willReturn(Optional.of(stored));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginOnUnknownUserReturnsSameStatusAsWrongPassword() throws Exception {
        given(repository.findByUsernameIgnoreCase("ghost")).willReturn(Optional.empty());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"password\":\"whatever1\"}"))
                .andExpect(status().isUnauthorized());
    }
}
