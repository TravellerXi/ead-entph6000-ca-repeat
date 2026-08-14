package ie.tudublin.ead.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Publishes recipe lifecycle events so review-service can react asynchronously.
 * Failures are swallowed: a broker outage must not make the REST API unavailable.
 */
@Component
public class RecipeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RecipeEventPublisher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final String exchange;

    public RecipeEventPublisher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
                                @Value("${app.messaging.exchange:ead.events}") String exchange) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.exchange = exchange;
    }

    public void publishCreated(Recipe recipe) {
        publish("recipe.created", Map.of(
                "recipeId", recipe.getId(),
                "name", recipe.getName(),
                "occurredAt", Instant.now().toString()));
    }

    public void publishDeleted(String recipeId) {
        publish("recipe.deleted", Map.of(
                "recipeId", recipeId,
                "occurredAt", Instant.now().toString()));
    }

    private void publish(String routingKey, Map<String, Object> payload) {
        RabbitTemplate template = rabbitTemplateProvider.getIfAvailable();
        if (template == null) {
            log.debug("RabbitMQ not configured; skipping event {}", routingKey);
            return;
        }
        try {
            template.convertAndSend(exchange, routingKey, payload);
            log.info("published {} {}", routingKey, payload);
        } catch (AmqpException e) {
            log.warn("failed to publish {}: {}", routingKey, e.getMessage());
        }
    }
}
