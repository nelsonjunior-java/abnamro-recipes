package com.abnamro.recipes_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Recipe-api app").version("1.0").description("API for managing recipes and ingredients"));
    }

    @Bean
    public OperationCustomizer customizePagination() {
        return (operation, handlerMethod) -> {
            if (handlerMethod.getMethod().getName().equals("getAllIngredients")) {
                Parameter pageParam = new Parameter().in("query").name("page")
                        .description("Page number (0-based)").required(false)
                        .schema(new io.swagger.v3.oas.models.media.IntegerSchema()._default(0));
                Parameter sizeParam = new Parameter().in("query").name("size")
                        .description("Page size").required(false)
                        .schema(new io.swagger.v3.oas.models.media.IntegerSchema()._default(10));
                Parameter sortParam = new Parameter().in("query").name("sort")
                        .description("Sort criteria in the format: property(,asc|desc). Default sort order is descending. Multiple sort criteria are supported.")
                        .required(false)
                        .schema(new io.swagger.v3.oas.models.media.StringSchema()._default("id,desc"));

                operation.addParametersItem(pageParam);
                operation.addParametersItem(sizeParam);
                operation.addParametersItem(sortParam);
            }
            return operation;
        };
    }
}
