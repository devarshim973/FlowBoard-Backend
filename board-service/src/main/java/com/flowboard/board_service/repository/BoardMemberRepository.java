package com.flowboard.board_service.repository;

import com.flowboard.board_service.entity.BoardMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer> {
    boolean existsByBoardIdAndUserId(Integer boardId, Integer userId);

    Optional<BoardMember> findByBoardIdAndUserId(Integer boardId, Integer userId);

    Page<BoardMember> findByBoardId(Integer boardId, Pageable pageable);

    /*
        Here we need to user query method as we only want to return the id of board memeber
         not the full entity
    */
    @Query("""
    SELECT bm.userId FROM BoardMember bm
    WHERE bm.boardId = :boardId
    """)
    Page<Integer> findUserIdsByBoardId(Integer boardId, Pageable pageable);
}