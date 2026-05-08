package com.flowboard.workspace_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;


/*
here the unique constraint is important to not allow duplicated user in a group
or you need check when inserting in the table every time
 */
@Entity
@Table(
        //optional, jps also does similar job camelCase -> snake_case but here we have extra s
        name = "workspace_members",
        indexes = {
                @Index(name = "idx_workspace", columnList = "workspaceId"),
                @Index(name = "idx_user", columnList = "userId")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"workspaceId", "userId"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberId;

    @Column(nullable = false)
    private Integer workspaceId;

    @Column(nullable = false)
    private Integer userId;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}