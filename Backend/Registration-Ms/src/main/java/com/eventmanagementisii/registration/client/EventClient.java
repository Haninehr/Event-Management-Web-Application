package com.eventmanagementisii.registration.client;

import java.time.LocalDate;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", url = "${app.event-service-url}")
public interface EventClient {
    @GetMapping("/events/{id}")
    EventDto getEvent(@PathVariable("id") Long id);

    // THIS MUST MATCH THE ENUM IN EVENT-MS !!
    enum EventStatus {
        ACTIVE, CANCELED
        // ENDED is derived, never sent over the wire
    }
    record EventDto(Long id, String title , Integer maxcapacity,
        LocalDate eventDate,        // ← to check if past
            EventStatus status , Long organizerId ) {}
}