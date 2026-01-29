package com.eventmanagementisii.event.repository;

import com.eventmanagementisii.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String desc);

    List<Event> findByLocationContainingIgnoreCase(String location);

    List<Event> findByEventDate(LocalDate date);



    
}