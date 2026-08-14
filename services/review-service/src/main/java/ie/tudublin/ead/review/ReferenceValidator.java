package ie.tudublin.ead.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Synchronous reference checks against recipe-service and user-service.
 * This is the inter-service call path demonstrated in the architecture overview.
 */
@Component
public class ReferenceValidator {

    private static final Logger log = LoggerFactory.getLogger(ReferenceValidator.class);

    private final RestClient recipeClient;
    private final RestClient userClient;

    public ReferenceValidator(RestClient.Builder builder,
                              @Value("${app.services.recipe-base-url}") String recipeBaseUrl,
                              @Value("${app.services.user-base-url}") String userBaseUrl) {
        this.recipeClient = builder.clone().baseUrl(recipeBaseUrl).build();
        this.userClient = builder.clone().baseUrl(userBaseUrl).build();
    }

    public boolean recipeExists(String recipeId) {
        return exists(recipeClient, "/api/recipes/{id}/exists", recipeId, "recipe-service");
    }

    public boolean userExists(String userId) {
        return exists(userClient, "/api/users/{id}/exists", userId, "user-service");
    }

    @SuppressWarnings("unchecked")
    private boolean exists(RestClient client, String path, String id, String target) {
        try {
            Map<String, Boolean> body = client.get()
                    .uri(path, id)
                    .retrieve()
                    .body(Map.class);
            return body != null && Boolean.TRUE.equals(body.get("exists"));
        } catch (RestClientException e) {
            // Fail closed: an unverifiable reference must not be persisted.
            log.warn("reference check against {} failed for id={}: {}", target, id, e.getMessage());
            return false;
        }
    }
}
