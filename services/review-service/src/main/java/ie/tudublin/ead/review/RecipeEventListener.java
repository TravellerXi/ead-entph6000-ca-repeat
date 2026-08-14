package ie.tudublin.ead.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Keeps reviews consistent with recipe lifecycle without recipe-service having to
 * call this service synchronously. Removing the coupling is the point of the queue.
 */
@Component
public class RecipeEventListener {

    private static final Logger log = LoggerFactory.getLogger(RecipeEventListener.class);

    private final ReviewRepository repository;

    public RecipeEventListener(ReviewRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "${app.messaging.queue:review.recipe-events}")
    public void onRecipeEvent(Map<String, Object> event) {
        Object recipeId = event.get("recipeId");
        if (recipeId == null) {
            log.warn("ignoring recipe event without recipeId: {}", event);
            return;
        }
        // Only deletions require action; creations are informational.
        if (event.containsKey("name")) {
            log.info("observed recipe.created for recipeId={}", recipeId);
            return;
        }
        long removed = repository.deleteByRecipeId(recipeId.toString());
        log.info("recipe.deleted recipeId={} cascaded removal of {} review(s)", recipeId, removed);
    }
}
