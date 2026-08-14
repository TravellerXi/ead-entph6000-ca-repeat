package ie.tudublin.ead.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewRepository repository;

    @MockBean
    private ReferenceValidator referenceValidator;

    @Test
    void createsReviewWhenBothReferencesResolve() throws Exception {
        Review incoming = new Review("r1", "u1", 5, "Excellent");
        Review persisted = new Review("r1", "u1", 5, "Excellent");
        persisted.setId("rev1");

        given(referenceValidator.recipeExists("r1")).willReturn(true);
        given(referenceValidator.userExists("u1")).willReturn(true);
        given(repository.save(any(Review.class))).willReturn(persisted);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("rev1"));
    }

    @Test
    void rejectsReviewForUnknownRecipe() throws Exception {
        given(referenceValidator.recipeExists("ghost")).willReturn(false);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Review("ghost", "u1", 4, "ok"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejectsReviewForUnknownAuthor() throws Exception {
        given(referenceValidator.recipeExists("r1")).willReturn(true);
        given(referenceValidator.userExists("ghost")).willReturn(false);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Review("r1", "ghost", 4, "ok"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejectsRatingOutOfRange() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Review("r1", "u1", 9, "too high"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void computesAverageRatingSummary() throws Exception {
        given(repository.findByRecipeId("r1")).willReturn(List.of(
                new Review("r1", "u1", 5, "great"),
                new Review("r1", "u2", 4, "good"),
                new Review("r1", "u3", 3, "fine")));

        mockMvc.perform(get("/api/reviews/summary/r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCount").value(3))
                .andExpect(jsonPath("$.averageRating").value(4.0));
    }

    @Test
    void filtersReviewsByRecipeId() throws Exception {
        given(repository.findByRecipeId("r1")).willReturn(List.of(new Review("r1", "u1", 5, "great")));

        mockMvc.perform(get("/api/reviews").param("recipeId", "r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipeId").value("r1"));
    }
}
