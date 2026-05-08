package com.flowboard.card_service.repository;

import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
    List<Card> findByListIdOrderByPositionAsc(Integer listId);

    List<Card> findByBoardIdOrderByPosition(Integer boardId);

    List<Card> findByAssigneeId(Integer assigneeId);

    Integer countByListId(Integer listId);

    List<Card> findByPriority(Priority priority);

    List<Card> findByStatus(Status status);

    List<Card> findByDueDateBefore(LocalDateTime date);

    List<Card> findByListIdAndIsArchivedFalseOrderByPosition(Integer listId);

    List<Card> findByBoardIdAndIsArchivedFalse(Integer boardId);

    @Query("SELECT COALESCE(MAX(c.position), 0) from Card c where c.listId = :listId AND c.isArchived = false")
    public Integer maxPosition(@Param("listId") Integer listId);

    List<Card> findByDueDateBeforeAndStatusNot(LocalDateTime now, Status status);

    List<Card> findByAssigneeIdAndDueDateBeforeAndStatusNotOrderByPositionAsc(Integer userId, LocalDateTime date, Status status);

    List<Card> findByIsArchivedFalse();
}