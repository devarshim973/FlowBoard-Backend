package com.flowboard.workspace_service.dto;

import com.flowboard.workspace_service.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceRequestDto {
    @Schema(description = "Workspace name", example = "Development Team")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Workspace name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Workspace description", example = "Workspace for managing development tasks")
    @NotBlank(message = "Description cannot be blank")
    @Size(min = 1, max = 100, message = "Workspace description must be between 1 and 100 characters")
    private String description;

    /*
    This is throw exception while parsing if visibility is not among PUBLIC or PRIVATE (static objects
    defined in enum) so no need for pattern or anything extra
     */
    @Schema(description = "Workspace visibility", example = "PUBLIC")
    @NotNull(message = "visibility must be PUBLIC or PRIVATE")
    private Visibility visibility;

    @Schema(description = "Workspace logo URL", example = "https://cdn.app.com/logo.png")
    @NotBlank(message = "Logo URL cannot be blank")
    @Size(min = 2, max = 255, message = "Workspace logo URL must be between 2 and 255 characters")
    private String logoUrl;
}
