package com.flowboard.workspace_service.dto;

import com.flowboard.workspace_service.entity.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceRequestDto {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 to 100 character")
    private String name;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 to 100 character")
    private String description;

    /*
    This is throw exception while parsing if visibility is not among PUBLIC or PRIVATE (static objects
    defined in enum) so no need for pattern or anything extra
     */
    @NotNull(message = "visibility must be PUBLIC or PRIVATE")
    private Visibility visibility;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 to 100 character")
    private String logoUrl;
}