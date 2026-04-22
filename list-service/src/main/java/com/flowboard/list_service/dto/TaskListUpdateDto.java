package com.flowboard.list_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskListUpdateDto {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 to 100 characters")
    private String name;

    /*
     UI color (hex code) for each color
    */
    @NotBlank(message = "String color is required")
    private String color;
}
