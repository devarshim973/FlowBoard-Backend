package com.flowboard.workspace_service.repository;

import com.flowboard.workspace_service.entity.Visibility;
import com.flowboard.workspace_service.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {

    Page<Workspace> findByOwnerId(Integer ownerId, Pageable pageable);

    boolean existsByNameAndOwnerId(String name, Integer ownerId);

    Page<Workspace> findByVisibility(Visibility visibility, Pageable pageable);

    Page<Workspace> findByWorkspaceIdIn(List<Integer> workspaceIds, Pageable pageable);
}