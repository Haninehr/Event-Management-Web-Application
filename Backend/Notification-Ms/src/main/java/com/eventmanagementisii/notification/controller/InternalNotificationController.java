package com.eventmanagementisii.notification.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventmanagementisii.notification.service.*;


@RestController
@RequestMapping("/Internal")
public class InternalNotificationController {

    private final NotificationService notificationService;

    public InternalNotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }


 
     
    @PostMapping("/triggerRegistration")
    public ResponseEntity<Void> triggerRegistrationNotification(@RequestBody Map<String, Long> payload) {
    
        notificationService.onReg(payload.get("userId"), payload.get("eventId"));
        return ResponseEntity.ok().build();
    }

    
    @PostMapping("/triggerRegistrationDecision")
    public ResponseEntity<Void> triggerRegistrationDecisionNotification(@RequestBody Map<String, Object> payload) {

        Long userId = payload.get("userId") instanceof Number n ? n.longValue() : null;
        Long eventId = payload.get("eventId") instanceof Number n ? n.longValue() : null;
        Boolean accepted = (Boolean) payload.get("accepted");

        if (accepted == null || userId == null || eventId == null) {
            return ResponseEntity.badRequest().build();
        }

        if (accepted) {
            notificationService.onRegAccepted(userId, eventId);
        } else {
            notificationService.onRegRefused(userId, eventId);
        }

        return ResponseEntity.ok().build();
    }

    

    
    @PostMapping("/triggerUnregistration")
    public ResponseEntity<Void> triggerUnregistrationNotification(@RequestBody Map<String, Long> payload) {
        notificationService.onUnreg(payload.get("userId"), payload.get("eventId"));
        return ResponseEntity.ok().build();
    }

    


    @PostMapping("/triggerEventUpdate")
    public ResponseEntity<Void> triggerEventUpdateNotification(@RequestBody Map<String, Long> payload) {
        Long eventId = payload.get("eventId");
        notificationService.onUpdate(eventId);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/triggerEventCancel")
    public ResponseEntity<Void> triggerEventCancelNotification(@RequestBody Map<String, Long> payload) {
        Long eventId = payload.get("eventId");
        notificationService.onCancel(eventId);
        return ResponseEntity.ok().build();
    }
    
}
