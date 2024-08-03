package com.abnamro.recipes_api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateIngredientRequest {

//    @NotBlank(message = "{ingredient.notBlank}")
//    @Size(max = ValidationConfig.MAX_LENGTH_NAME, message = "{ingredient.size}")
//    @Pattern(regexp = ValidationConfig.PATTERN_NAME, message = "{ingredient.pattern}")
//    @ApiModelProperty(notes = "The ingredient name", example = "Onion")
    private String name;
}
