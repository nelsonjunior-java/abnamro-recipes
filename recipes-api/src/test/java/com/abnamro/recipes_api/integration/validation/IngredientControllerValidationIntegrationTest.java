package com.abnamro.recipes_api.integration.validation;

import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.integration.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import java.util.stream.Stream;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IngredientControllerValidationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("abnamro")
            .withUsername("admin")
            .withPassword("admin");

    @Container
    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management");

    private static Stream<CreateIngredientRequestTestData> createIngredientRequestTestData() {
        return Stream.of(
                new CreateIngredientRequestTestData("Ingredient name must not be blank.", "", "name"),
                new CreateIngredientRequestTestData("Ingredient name must not exceed 255 characters.", "A".repeat(256), "name")
        );
    }

    @ParameterizedTest
    @MethodSource("createIngredientRequestTestData")
    public void testCreateIngredientValidation(CreateIngredientRequestTestData testData) throws Exception {
        final CreateIngredientRequest request = createCreateIngredientRequest(testData);
        final MvcResult result = mockMvc.perform(post("/api/v1/ingredient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Validate the error message is related to the specific field
        final String content = result.getResponse().getContentAsString();
        assertTrue("Expected error message not found for field: " + testData.getFieldName(), content.contains(testData.getExpectedError()));
    }

    private CreateIngredientRequest createCreateIngredientRequest(CreateIngredientRequestTestData testData) {
        CreateIngredientRequest request = new CreateIngredientRequest();
        request.setName(testData.getName());
        return request;
    }

    private static class CreateIngredientRequestTestData {
        private final String expectedError;
        private final String name;
        private final String fieldName;

        public CreateIngredientRequestTestData(String expectedError, String name, String fieldName) {
            this.expectedError = expectedError;
            this.name = name;
            this.fieldName = fieldName;
        }

        public String getExpectedError() {
            return expectedError;
        }

        public String getName() {
            return name;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}
