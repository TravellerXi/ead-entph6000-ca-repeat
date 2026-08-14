package ie.tudublin.ead.recipe;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends MongoRepository<Recipe, String> {

    Optional<Recipe> findByNameIgnoreCase(String name);

    List<Recipe> findByPrepTimeInMinutesLessThanEqual(int maxMinutes);

    boolean existsByNameIgnoreCase(String name);
}
