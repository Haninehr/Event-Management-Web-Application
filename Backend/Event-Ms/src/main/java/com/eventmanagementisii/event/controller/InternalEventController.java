package com.eventmanagementisii.event.controller;
import com.eventmanagementisii.event.service.*;

import java.time.LocalDate;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Internal")
public class InternalEventController {

    private final EventService eventService;

    public InternalEventController (EventService eventService ){
        this.eventService = eventService;
    }



    @GetMapping("/upcoming")
    public List<EventDto> getUpcoming(@RequestParam String date) {
        LocalDate eventDate = LocalDate.parse(date);
        return eventService.findByEventDate(eventDate)
                .stream()
                .map(event -> new EventDto(
                        event.getId(),
                        event.getTitle(),
                        event.getEventDate().toString(),
                        event.getEventTime().toString(),
                        event.getLocation()
                ))
                .toList();
    }



@GetMapping("/{id}/organizer")
public ResponseEntity<Long> getOrganizerId(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.getOrganizerId(id));
}
 

 // record inside controller
    public record EventDto(Long id, String title, String eventDate, String eventTime, String location) {}
    
}
