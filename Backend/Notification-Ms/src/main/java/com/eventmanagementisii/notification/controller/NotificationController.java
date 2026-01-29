package com.eventmanagementisii.notification.controller;

import com.eventmanagementisii.notification.entity.Notification;
import com.eventmanagementisii.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // get user notification !
    @GetMapping
public ResponseEntity<List<Notification>> getForUser(@RequestParam Long userId) {
    return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
}

    // mark as read
    @PatchMapping("/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

   
}
