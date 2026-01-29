package com.eventmanagementisii.event.controller;

import com.eventmanagementisii.event.Dto.*;
import com.eventmanagementisii.event.entity.Event;
import com.eventmanagementisii.event.service.EventService;

import com.eventmanagementisii.event.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventRepository eventRepository;


    private Long getCurrentUserId(Authentication auth) {
        return Long.valueOf(auth.getName());
        
    }

   
private void checkIsEventOrganizer(Long eventId, Long userId) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

    if (event == null || !event.getOrganizerId().equals(userId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
    }
}

    // -------------------------
    // ORGANIZER ONLY
    // -------------------------
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse create(@RequestBody EventRequest request, Authentication auth) {
        return eventService.create(request, auth);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse update(@PathVariable Long id, @RequestBody EventRequest request, Authentication auth) {

         checkIsEventOrganizer(id, getCurrentUserId(auth));
        return eventService.update(id, request, auth);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Event> cancel_event(@PathVariable Long id, Authentication auth) {

        checkIsEventOrganizer(id, getCurrentUserId(auth));
        eventService.cancel_event(id, auth);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/media")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String uploadMedia(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String mediaType,
            @RequestParam("title") String title,
            Authentication auth) throws Exception {

                checkIsEventOrganizer(id, getCurrentUserId(auth));
        return eventService.uploadMedia(id, file, mediaType,title, auth);
    }


    @DeleteMapping("/{eventId}/media")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String deleteMedia(@PathVariable Long eventId, @RequestParam("mediaUrl") String mediaUrl ,Authentication auth)
    {
         checkIsEventOrganizer(eventId, getCurrentUserId(auth));
         return eventService.deleteMedia(eventId,mediaUrl);

    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ORGANIZER')")
    public StatsResponse stats(@PathVariable Long id, Authentication auth) {
        checkIsEventOrganizer(id, getCurrentUserId(auth));
        return eventService.getStats(id, auth);
    }


    // -------------------------
    // PUBLIC GET
    // -------------------------
    @GetMapping
    public List<EventResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
            ) {
        return eventService.search(keyword, location, date);
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) { 
        return eventService.getById(id);
    }



 // record inside controller
    public record EventDto(Long id, String title, String eventDate, String eventTime, String location) {}
}
