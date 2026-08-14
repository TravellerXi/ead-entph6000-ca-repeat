package ie.tudublin.ead.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 40)
    @Indexed(unique = true)
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Indexed(unique = true)
    private String email;

    /** Accepted on write, never serialised back out. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private transient String password;

    /** BCrypt digest. Never leaves the service. */
    @JsonIgnore
    private String passwordHash;

    private String displayName;

    private Instant createdAt = Instant.now();

    public User() {
    }

    public User(String username, String email, String displayName) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
