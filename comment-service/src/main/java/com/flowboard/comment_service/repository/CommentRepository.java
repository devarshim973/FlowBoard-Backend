package com.flowboard.comment_service.repository;

import com.flowboard.comment_service.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Page<Comment> findByCardId(Integer cardId, Pageable pageable);

    Page<Comment> findByAuthorId(Integer authorId, Pageable pageable);

    Page<Comment> findByParentCommentId(Integer parentCommentId, Pageable pageable);

    Long countByCardId(Integer cardId);
}
