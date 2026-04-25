package com.flowboard.workspace_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer workspaceId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer ownerId; // user who created workspace

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    private String logoUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}