package ie.tudublin.ead.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String instanceColour;

    public UserController(UserRepository repository,
                          @Value("${app.instance-colour:blue}") String instanceColour) {
        this.repository = repository;
        this.instanceColour = instanceColour;
    }

    @GetMapping("/_meta")
    public Map<String, String> meta() {
        return Map.of("service", "user-service", "instanceColour", instanceColour);
    }

    @GetMapping
    public List<User> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody User user) {
        if (repository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already taken");
        }
        if (repository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setPassword(null);
        User saved = repository.save(user);
        // Never log credentials or the digest.
        log.info("registered user id={} username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        User user = repository.findByUsernameIgnoreCase(request.username())
                // Same response for unknown user and wrong password: avoids username enumeration.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        return Map.of("userId", user.getId(), "username", user.getUsername());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Called by review-service to validate an author reference before accepting a review. */
    @GetMapping("/{id}/exists")
    public Map<String, Boolean> exists(@PathVariable String id) {
        return Map.of("exists", repository.existsById(id));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
