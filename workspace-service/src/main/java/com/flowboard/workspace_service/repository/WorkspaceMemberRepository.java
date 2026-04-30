package com.flowboard.workspace_service.repository;

import com.flowboard.workspace_service.entity.WorkspaceMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer> {
    Page<WorkspaceMember> findByWorkspaceId(Integer workspaceId, Pageable pageable);

    Page<WorkspaceMember> findByUserId(Integer userId, Pageable pageable);

    boolean existsByWorkspaceIdAndUserId(Integer workspaceId, Integer userId);

    void deleteByWorkspaceIdAndUserId(Integer workspaceId, Integer userId);
}