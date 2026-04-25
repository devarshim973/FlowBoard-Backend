package com.flowboard.board_service.repository;

import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.entity.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    Page<Board> findByWorkspaceId(Integer workspaceId, Pageable pageable);

    boolean existsByNameAndWorkspaceId(String name, Integer workspaceId);

    Page<Board> findByWorkspaceIdAndVisibility(Integer workspaceId, Visibility visibility, Pageable pageable);

    @Query("""
    SELECT b FROM Board b
    JOIN BoardMember bm ON b.boardId = bm.boardId
    WHERE b.workspaceId = :workspaceId
    AND b.visibility = com.flowboard.board_service.entity.Visibility.PRIVATE
    AND bm.userId = :userId
    """)
    Page<Board> findPrivateBoardsByWorkspaceAndUser(Integer workspaceId,  Integer userId, Pageable pageable);
}