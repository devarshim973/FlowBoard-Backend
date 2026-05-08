package com.flowboard.card_service.repository;

import com.flowboard.card_service.entity.ActivityType;
import com.flowboard.card_service.entity.CardActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardActivityRepository extends JpaRepository<CardActivity, Integer> {
    Page<CardActivity> findByCardIdOrderByCreatedAtDesc(Integer cardId, Pageable pageable);

    Page<CardActivity> findByActorIdOrderByCreatedAtDesc(Integer actorId, Pageable pageable);

    Page<CardActivity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<CardActivity> findByActivityTypeOrderByCreatedAtDesc(ActivityType activityType, Pageable pageable);
}
