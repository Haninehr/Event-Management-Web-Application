package com.eventmanagementisii.registration.controller;
import com.eventmanagementisii.registration.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/Internal")
public class InternalRegistrationControl {
    
    private final RegistrationService RegService; // ← whatever your service is called

    public InternalRegistrationControl(RegistrationService RegService) {
        this.RegService = RegService;
    }

    //do internal batch ! (to comunicate with registration ms)
    // This endpoint is ONLY called by other microservices (registration-ms)
    /*@GetMapping("/users/batch")
    public Map<Long, UserInfo> getUsersByIds(@RequestParam List<Long> userIds) {
        return userService.getPublicInfoByIds(userIds);
    }*/

        //@PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{eventId}/registeredUsers")
    public ResponseEntity<List<Long>> getRegisteredUserIds(@PathVariable Long eventId) {
           // checkIsEventOrganizer(eventId, getCurrentUserId(auth));
        return ResponseEntity.ok(RegService.getRegisteredUserIds(eventId));
    }

}




