package ie.tudublin.ead.recipe;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "recipes")
public class Recipe {

    @Id
    private String id;

    @NotBlank(message = "name is required")
    @Indexed(unique = true)
    private String name;

    @NotEmpty(message = "at least one ingredient is required")
    private List<String> ingredients;

    @Min(value = 1, message = "prepTimeInMinutes must be positive")
    private int prepTimeInMinutes;

    private Instant createdAt = Instant.now();

    public Recipe() {
    }

    public Recipe(String name, List<String> ingredients, int prepTimeInMinutes) {
        this.name = name;
        this.ingredients = ingredients;
        this.prepTimeInMinutes = prepTimeInMinutes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public int getPrepTimeInMinutes() {
        return prepTimeInMinutes;
    }

    public void setPrepTimeInMinutes(int prepTimeInMinutes) {
        this.prepTimeInMinutes = prepTimeInMinutes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Recipe{id='" + id + "', name='" + name + "', ingredients=" + ingredients
                + ", prepTimeInMinutes=" + prepTimeInMinutes + "}";
    }
}
