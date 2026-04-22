package com.flowboard.board_service.dto;

import com.flowboard.board_service.entity.Visibility;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BoardRequestDto {
    @NotNull(message = "Workspace id cannot be null")
    private Integer workspaceId;

    @NotBlank(message = "Board name cannot be blank")
    @Size(min = 2, max = 100, message = "Board name must be between 2 to 100 characters")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    private String background;

    @NotNull(message = "Visibility must be PUBLIC or PRIVATE")
    private Visibility visibility;
}