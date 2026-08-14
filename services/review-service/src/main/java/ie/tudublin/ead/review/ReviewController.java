package ie.tudublin.ead.review;

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
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewRepository repository;
    private final ReferenceValidator referenceValidator;
    private final String instanceColour;

    public ReviewController(ReviewRepository repository,
                            ReferenceValidator referenceValidator,
                            @Value("${app.instance-colour:blue}") String instanceColour) {
        this.repository = repository;
        this.referenceValidator = referenceValidator;
        this.instanceColour = instanceColour;
    }

    @GetMapping("/_meta")
    public Map<String, String> meta() {
        return Map.of("service", "review-service", "instanceColour", instanceColour);
    }

    @GetMapping
    public List<Review> findAll(@RequestParam(required = false) String recipeId,
                                @RequestParam(required = false) String authorId) {
        if (recipeId != null) {
            return repository.findByRecipeId(recipeId);
        }
        if (authorId != null) {
            return repository.findByAuthorId(authorId);
        }
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Review findById(@PathVariable String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "review not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review create(@Valid @RequestBody Review review) {
        if (!referenceValidator.recipeExists(review.getRecipeId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "unknown recipeId: " + review.getRecipeId());
        }
        if (!referenceValidator.userExists(review.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "unknown authorId: " + review.getAuthorId());
        }
        Review saved = repository.save(review);
        log.info("created review id={} recipeId={} rating={}", saved.getId(), saved.getRecipeId(), saved.getRating());
        return saved;
    }

    @GetMapping("/summary/{recipeId}")
    public Map<String, Object> summary(@PathVariable String recipeId) {
        List<Review> reviews = repository.findByRecipeId(recipeId);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return Map.of(
                "recipeId", recipeId,
                "reviewCount", reviews.size(),
                "averageRating", Math.round(average * 100.0) / 100.0);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "review not found: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
