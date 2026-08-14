package ie.tudublin.ead.review;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByRecipeId(String recipeId);

    List<Review> findByAuthorId(String authorId);

    long deleteByRecipeId(String recipeId);
}
