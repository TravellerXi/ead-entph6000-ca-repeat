package ie.tudublin.ead.recipe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipeRepository repository;

    @MockBean
    private RecipeEventPublisher eventPublisher;

    @Test
    void returnsAllRecipes() throws Exception {
        given(repository.findAll()).willReturn(List.of(new Recipe("elotes", List.of("corn", "lime"), 35)));

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("elotes"))
                .andExpect(jsonPath("$[0].prepTimeInMinutes").value(35));
    }

    @Test
    void createsRecipeAndPublishesEvent() throws Exception {
        Recipe incoming = new Recipe("fried rice", List.of("rice", "egg"), 40);
        Recipe persisted = new Recipe("fried rice", List.of("rice", "egg"), 40);
        persisted.setId("abc123");

        given(repository.existsByNameIgnoreCase("fried rice")).willReturn(false);
        given(repository.save(any(Recipe.class))).willReturn(persisted);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc123"));

        verify(eventPublisher).publishCreated(persisted);
    }

    @Test
    void rejectsDuplicateRecipeName() throws Exception {
        given(repository.existsByNameIgnoreCase("elotes")).willReturn(true);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Recipe("elotes", List.of("corn"), 35))))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidPayload() throws Exception {
        // blank name and empty ingredients must fail bean validation
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Recipe("", List.of(), 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownId() throws Exception {
        given(repository.findById("missing")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/recipes/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void exposesInstanceColourForBlueGreenDemo() throws Exception {
        mockMvc.perform(get("/api/recipes/_meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("recipe-service"));
    }
}
