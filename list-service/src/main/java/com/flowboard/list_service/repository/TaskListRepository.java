package com.flowboard.list_service.repository;

import com.flowboard.list_service.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
As we need kanban board we always need to return all the board with sorted(ordered) on
the basis of position so here we are not using pagination
 */
@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Integer> {
    /*
    List all the list(archived + non-archived)
     */
    List<TaskList> findByBoardIdOrderByPosition(Integer boardId);

    List<TaskList> findByBoardIdAndArchivedFalseOrderByPosition(Integer boardId);

    List<TaskList> findByBoardIdAndArchivedTrueOrderByPosition(Integer boardId);

    Integer countByBoardId(Integer boardId);

    Integer countByBoardIdAndArchived(Integer boardId, Boolean archived);

    Optional<TaskList> findByBoardIdAndPosition(Integer boardId, Integer position);

    /*
    Here the coalesce is used -> if there are no list you will get null so this will
    return 0 if there are no position and then we can do 0+1 and start our position from 1
     */
    @Query("SELECT COALESCE(MAX(t.position), 0) from TaskList t where t.boardId = :boardId AND t.archived = false")
    public Integer maxPosition(@Param("boardId") Integer boardId);
}
