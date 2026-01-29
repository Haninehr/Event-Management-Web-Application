package com.eventmanagementisii.registration.controller;

import com.eventmanagementisii.registration.client.EventClient;
//import com.eventmanagementisii.registration.client.UserClient;
import com.eventmanagementisii.registration.service.RegistrationService;
//import com.eventmanagementisii.registration.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.eventmanagementisii.registration.Dto.ParticipantDto;
//import java.time.LocalDateTime;
import java.util.List;
 
@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final EventClient eventClient;
    //private final UserClient userclient;

    private Long getCurrentUserId(Authentication auth) {
        return Long.valueOf(auth.getName());
        
    }

    private void checkIsEventOrganizer(Long eventId, Long userId) {
        var event = eventClient.getEvent(eventId);
        if (event == null || !event.organizerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas l'organisateur de cet événement.");
        }
    }

    // ==================== USER ACTIONS ====================

    @PostMapping("/{eventId}")
    public ResponseEntity<String> register(
            @PathVariable Long eventId,
            Authentication auth) {

        String result = registrationService.register(eventId, auth);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    //done
    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> unregister(
            @PathVariable Long eventId,
            Authentication auth) {

        String result = registrationService.unregister(eventId, auth);
        return ResponseEntity.ok(result);
    }

    //done
    @GetMapping("/{eventId}/status")
    public ResponseEntity<String> getRegistrationStatus(
            @PathVariable Long eventId,
            Authentication auth) {

        String status = registrationService.getRegistrationStatus(eventId, auth);
        return ResponseEntity.ok(status);
    }

    //done
    @GetMapping("/{eventId}/registered")
    public ResponseEntity<Boolean> isRegistered(
            @PathVariable Long eventId,
            Authentication auth) {

        boolean registered = registrationService.isRegistered(eventId, auth);
        return ResponseEntity.ok(registered);
    }

    


    // ==================== ORGANIZER ACTIONS ====================

    @PostMapping("/{eventId}/accept/{userId}")
    //@PreAuthorize("hasRole('ORGANIZER')") // optional extra layer
    public ResponseEntity<String> acceptParticipant(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            Authentication auth) {

        if (userId == null || userId <= 0) {
        return ResponseEntity.badRequest().body("Utilisateur invalide");
        }

        checkIsEventOrganizer(eventId, getCurrentUserId(auth));
        String result = registrationService.acceptParticipant(eventId, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{eventId}/refuse/{userId}")
    //@PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> refuseParticipant(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            Authentication auth) {

        checkIsEventOrganizer(eventId, getCurrentUserId(auth));
        String result = registrationService.refuseParticipant(eventId, userId);
        return ResponseEntity.ok(result);
    }

    // ==================== PUBLIC / ORGANIZER INFO ====================
    
    //no need authentification
    @GetMapping("/{eventId}/participantsCount")
    public ResponseEntity<Integer> getParticipantCount(@PathVariable Long eventId) {
        int count = registrationService.getParticipantCount(eventId);
        return ResponseEntity.ok(count);
    }

    //@PreAuthorize("hasRole('ORGANIZER')")
    /*@GetMapping("/Internal/{eventId}/registeredUsers")
    public ResponseEntity<List<Long>> getRegisteredUserIds(@PathVariable Long eventId) {
           // checkIsEventOrganizer(eventId, getCurrentUserId(auth));
        return ResponseEntity.ok(registrationService.getRegisteredUserIds(eventId));
    }*/

    // Organizer dashboard endpoints

    //we need participant modal in frontend ti get the response !!! !
    //@PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{eventId}/pending")
    public ResponseEntity<List<ParticipantDto>> getPendingRegistrations(
            @PathVariable Long eventId,
            Authentication auth) {

        checkIsEventOrganizer(eventId, getCurrentUserId(auth));
        List<ParticipantDto> list = registrationService.getPendingRegistrations(eventId);

        System.out.println("RETURNING TO FRONTEND → " + list);  // LÀ ON VERRA LA VÉRITÉ
        return ResponseEntity.ok(list);
    }

    //@PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{eventId}/accepted")
    public ResponseEntity<List<ParticipantDto>> getAcceptedParticipants(
            @PathVariable Long eventId,
            Authentication auth) {

       checkIsEventOrganizer(eventId, getCurrentUserId(auth));
        List<ParticipantDto> list = registrationService.getAcceptedParticipants(eventId);
        return ResponseEntity.ok(list);
    }


     @GetMapping("/{eventId}/refused")
    public ResponseEntity<List<ParticipantDto>> getRefusedParticipants(
            @PathVariable Long eventId,
            Authentication auth) {

       
       checkIsEventOrganizer(eventId, getCurrentUserId(auth));
        List<ParticipantDto> list = registrationService.getRefusedRegistrations(eventId);
        return ResponseEntity.ok(list);
    }
    // Simple DTO for organizer dashboard
    //public record ParticipantDto(Long userId, String username,String email, String status, LocalDateTime registeredat) {}
}