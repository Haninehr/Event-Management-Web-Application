package com.eventmanagementisii.event.service;

import com.eventmanagementisii.event.Dto.*;
import com.eventmanagementisii.event.entity.Event;
import com.eventmanagementisii.event.entity.EventMedia;
import com.eventmanagementisii.event.entity.EventStatus;
import com.eventmanagementisii.event.repository.EventRepository;
import com.eventmanagementisii.event.repository.EventMediaRepository;
import com.eventmanagementisii.event.client.NotificationClient;
import lombok.RequiredArgsConstructor;


import lombok.extern.slf4j.Slf4j;


import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventMediaRepository mediaRepository;
    private final MediaService mediaService;
    private final NotificationClient notificationClient;

    // ========================================================================
    // CREATE EVENT (ORGANIZER ONLY)
    // ========================================================================
   
    @Transactional
    public EventResponse create(EventRequest request, Authentication auth) {
        Long organizerId = extractUserId(auth);
        
        if (LocalDate.now().isAfter(request.getEventDate())) {
            throw new SecurityException("Cannot create event with date passed!");
        }
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .location(request.getLocation())
                .type(request.getType())
                .organizerId(organizerId)
                .views(0)
                .maxCapacity(request.getMaxcapacity())

                .build();

        event = eventRepository.save(event);
        return toResponse(event);
    }

    // ========================================================================
    // SEARCH & FILTER (PUBLIC)


    // ========================================================================
    public List<EventResponse> search(String keyword, String location, LocalDate date) {
        List<Event> events = eventRepository.findAll();

        if (keyword != null && !keyword.trim().isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                            e.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (location != null && !location.trim().isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getLocation().toLowerCase().contains(location.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (date != null) {
            events = events.stream()
                    .filter(e -> e.getEventDate().equals(date))
                    .collect(Collectors.toList());
        }
        

        return events.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ========================================================================
    // GET EVENT BY ID (PUBLIC - increments views)
    // ========================================================================
    @Transactional
    public EventResponse getById(Long id) {
       
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        if (event.getCurrentStatus() == EventStatus.ACTIVE) {
            event.setViews(event.getViews() + 1);

        }

        eventRepository.save(event);
        return toResponse(event);
    }

    // ========================================================================
    // UPDATE EVENT (ORGANIZER & OWNER ONLY)
    // ========================================================================

@Transactional
public EventResponse update(Long id, EventRequest request, Authentication auth) {
    Long organizerId = extractUserId(auth);

    // 1. Load the current event (this triggers the first SELECT you see)
    
    Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found: " + id));

    if (!event.getOrganizerId().equals(organizerId)) {
        throw new SecurityException("You are not the organizer of this event");
    }

    if (event.getCurrentStatus() == EventStatus.CANCELED) {
        throw new IllegalStateException("Cannot update a canceled event");
    }

    // 2. Log the BEFORE state — this is GOLD for debugging
    log.info("=== BEFORE UPDATE ===");
    log.info("Current DB values → title: '{}', maxCapacity: {}, eventDate: {}", 
             event.getTitle(), event.getMaxCapacity(), event.getEventDate());

    // 3. Apply changes (with proper difference checks)
    boolean changed = false;

    if (request.getTitle() != null && !request.getTitle().isBlank()) {
        String newTitle = request.getTitle().trim();
        if (!newTitle.equals(event.getTitle())) {
            event.setTitle(newTitle);
            changed = true;
        }
    }

    if (request.getDescription() != null) {
        String newDesc = request.getDescription().isBlank() ? null : request.getDescription().trim();
        if (!newDesc.equals( event.getDescription())) {
            event.setDescription(newDesc);
            changed = true;
        }
    }

    if (request.getLocation() != null && !request.getLocation().isBlank()) {
        String newLoc = request.getLocation().trim();
        if (!newLoc.equals(event.getLocation())) {
            event.setLocation(newLoc);
            changed = true;
        }
    }

    if (request.getType() != null ) {
        if (!request.getType().equals(event.getType())) {
            event.setType(request.getType());
            changed = true;
        }
    }

    if (request.getEventTime() != null && !request.getEventTime().isBlank()) {
        if (!request.getEventTime().equals(event.getEventTime())) {
            event.setEventTime(request.getEventTime());
            changed = true;
        }
    }

    if (request.getEventDate() != null) {
        if (!request.getEventDate().equals(event.getEventDate())) {
            log.info("Changing date from {} → {}", event.getEventDate(), request.getEventDate());
            event.setEventDate(request.getEventDate());
            changed = true;
        }
    }

    if (request.getMaxcapacity() != null) {
        if (!request.getMaxcapacity().equals(event.getMaxCapacity())) {
            log.info("Changing maxCapacity from {} → {}", event.getMaxCapacity(), request.getMaxcapacity());
            event.setMaxCapacity(request.getMaxcapacity());
            changed = true;
        }
    }

    // 4. THIS IS THE KEY PART
    if (!changed) {
        log.warn("NO CHANGES DETECTED → Hibernate will NOT send UPDATE query (or it will be a no-op)");
        // Still return fresh data
        return toResponse(event);
    }

    log.info("Changes detected → calling save()");

    
    Event savedEvent = eventRepository.save(event);

    log.info("=== AFTER SAVE ===");
    log.info("Saved event in memory → title: '{}', maxCapacity: {}, eventDate: {} , eventTime: {}", 
             savedEvent.getTitle(), savedEvent.getMaxCapacity(), savedEvent.getEventDate());

    Map<String, Long> payload = Map.of("eventId", id);

    try {
    notificationClient.EventUpdateNotify(payload);
} catch (Exception e) {
    log.warn("Notification failed, but DB update must survive", e);
    
}
    

    return toResponse(savedEvent);
}
    // ========================================================================
    // DELETE EVENT (ORGANIZER & OWNER ONLY)


    // ========================================================================
    @Transactional
    public void cancel_event(Long id, Authentication auth) {
        Long organizerId = extractUserId(auth); // problem organizer id undfined !!
        log.info("trigger cancel event from events");
        log.info("oragnizerid:", organizerId);
        
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
        if (organizerId == null) {
            throw new SecurityException("Orgnizer not found !!");
        }
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new SecurityException("You are not the organizer of this event");
        }

     
        if (event.getCurrentStatus() == EventStatus.ENDED) {
            throw new IllegalStateException("Cannot cancel an already ended event");
        }

        if (event.getCurrentStatus() == EventStatus.CANCELED) {
            throw new IllegalStateException("Cannot cancel an already cancled event");
        }

        event.setStatus(EventStatus.CANCELED);
        // send notification to subscribed users only !!
        Map<String, Long> payload = Map.of("eventId", id);
        notificationClient.EventCancelNotify(payload);
       
        // eventRepository.delete(event);
        eventRepository.save(event);
    }


    //delete media
    
    @Transactional
    public String deleteMedia(Long eventId , String mediaUrl){
        // Find and validate the media belongs to the event
        Optional<EventMedia> optionalMedia = mediaRepository.findByFilePathAndEventId(mediaUrl, eventId);

    EventMedia media = optionalMedia.orElse(null);

    if(media == null){
        throw new RuntimeException("Média non trouvé ou n'appartient pas à cet événement");
    }
        if (!media.getEvent().getId().equals(eventId)) {
        throw new RuntimeException("this media does not belong to this event ! ");
    }

    // Optional: delete physical file   //lets keep it 
    // deleteFileFromStorage(media.getFilePath());

    // Delete from database
    mediaRepository.delete(media);
        return "Média supprimé avec succès !";
    }
    // ========================================================================
    // UPLOAD MEDIA (ORGANIZER & OWNER ONLY)


    // ========================================================================
   
    @Transactional
    public String uploadMedia(Long eventId, MultipartFile file, String mediaType, String title, Authentication auth)
            throws IOException {
        Long organizerId = extractUserId(auth);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new SecurityException("You are not the organizer of this event");
        }

        String fileName = mediaService.storeFile(file, mediaType);
        EventMedia media = EventMedia.builder()
                .event(event)
                .filePath(fileName)
                .mediaType(mediaType.toUpperCase())
                .title(title)
                .build();

        mediaRepository.save(media);
        return mediaService.getMediaUrl(fileName); //return url !
    }

    // ========================================================================
    // GET STATISTICS (ORGANIZER & OWNER ONLY)


    // ========================================================================
    public StatsResponse getStats(Long id, Authentication auth) {
        Long organizerId = extractUserId(auth);
        
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new SecurityException("You are not the organizer of this event");
        }

        StatsResponse stats = new StatsResponse();
        stats.setViews(event.getViews());

        
        

        return stats;
    }

    public List<Event> findByEventDate(LocalDate date) {
        return eventRepository.findByEventDate(date);
    }

   

   
    public Long getOrganizerId(Long id) {
        return eventRepository.findById(id)
                .map(Event::getOrganizerId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    // ========================================================================
    // UTILITY: Extract user ID from JWT



    // ========================================================================
    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {

            throw new SecurityException("Authentication required");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new SecurityException("Invalid user ID in token");
        }
    }

    
    private EventResponse toResponse(Event event) {
    
                List<MediaDto> mediaDtos = mediaRepository.findByEventId(event.getId()).stream()
    .map(media -> new MediaDto(
        mediaService.getMediaUrl(media.getFilePath()),
        media.getTitle()
    ))
    .collect(Collectors.toList());

        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setEventDate(event.getEventDate());
        response.setEventTime(event.getEventTime());
        response.setLocation(event.getLocation());
        response.setType(event.getType());
        response.setOrganizerId(event.getOrganizerId());
        response.setViews(event.getViews());
        response.setMedias(mediaDtos);
        response.setCreatedAt(event.getCreatedAt());
        response.setMaxcapacity(event.getMaxCapacity());
        response.setStatus(event.getCurrentStatus());
        return response;
    }



}