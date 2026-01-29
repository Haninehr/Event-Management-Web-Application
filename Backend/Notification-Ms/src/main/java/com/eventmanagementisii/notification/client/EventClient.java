package com.eventmanagementisii.notification.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service", url = "${app.event-service-url}")
public interface EventClient {

    @GetMapping("/events/{id}")
    EventDto getEvent(@PathVariable("id") Long id);

    @GetMapping("/Internal/upcoming")
List<EventDto> getUpcomingEvents(@RequestParam("date") String date); //date will be a paramter in url : ?date=2025-11-18

@GetMapping("/Internal/{id}/organizer")
Long getOrganizerId(@PathVariable("id") Long eventId);

    record EventDto(Long id, String title, String eventDate, String eventTime ,String location) {}
}