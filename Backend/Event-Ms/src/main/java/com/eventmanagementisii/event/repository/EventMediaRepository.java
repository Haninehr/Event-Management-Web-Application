package com.eventmanagementisii.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventmanagementisii.event.entity.EventMedia;

// EventMediaRepository.java
public interface EventMediaRepository extends JpaRepository<EventMedia, Long> {
    List<EventMedia> findByEventId(Long eventId);
    // This is the key: find by filePath + eventId for security
    Optional<EventMedia> findByFilePathAndEventId(String filePath, Long eventId);
}