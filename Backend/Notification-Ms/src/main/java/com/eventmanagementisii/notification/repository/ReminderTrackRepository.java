package com.eventmanagementisii.notification.repository;

import com.eventmanagementisii.notification.entity.ReminderTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReminderTrackRepository extends JpaRepository<ReminderTrack, Long> {
    Optional<ReminderTrack> findByEventIdAndUserId(Long eventId, Long userId);
}
