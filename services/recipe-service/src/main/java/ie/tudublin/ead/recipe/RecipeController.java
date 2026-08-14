package ie.tudublin.ead.recipe;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    private final RecipeRepository repository;
    private final RecipeEventPublisher eventPublisher;
    private final String instanceColour;

    public RecipeController(RecipeRepository repository,
                            RecipeEventPublisher eventPublisher,
                            @Value("${app.instance-colour:blue}") String instanceColour) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.instanceColour = instanceColour;
    }

    /** Surfaces which blue/green slot served the request, so the strategy switch is observable in the demo. */
    @GetMapping("/_meta")
    public Map<String, String> meta() {
        return Map.of("service", "recipe-service", "instanceColour", instanceColour);
    }

    @GetMapping
    public List<Recipe> findAll(@RequestParam(required = false) Integer maxPrepTime) {
        if (maxPrepTime != null) {
            return repository.findByPrepTimeInMinutesLessThanEqual(maxPrepTime);
        }
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Recipe findById(@PathVariable String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "recipe not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe create(@Valid @RequestBody Recipe recipe) {
        if (repository.existsByNameIgnoreCase(recipe.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "recipe already exists: " + recipe.getName());
        }
        Recipe saved = repository.save(recipe);
        eventPublisher.publishCreated(saved);
        log.info("created recipe id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @PutMapping("/{id}")
    public Recipe update(@PathVariable String id, @Valid @RequestBody Recipe recipe) {
        Recipe existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "recipe not found: " + id));
        existing.setName(recipe.getName());
        existing.setIngredients(recipe.getIngredients());
        existing.setPrepTimeInMinutes(recipe.getPrepTimeInMinutes());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "recipe not found: " + id);
        }
        repository.deleteById(id);
        eventPublisher.publishDeleted(id);
        log.info("deleted recipe id={}", id);
        return ResponseEntity.noContent().build();
    }

    /** Called by review-service to validate a recipe reference before accepting a review. */
    @GetMapping("/{id}/exists")
    public Map<String, Boolean> exists(@PathVariable String id) {
        return Map.of("exists", repository.existsById(id));
    }
}
