package com.eventmanagementisii.notification.client;

//import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "registration-service", url = "${app.registration-service-url}")
public interface RegistrationClient {

    /*@GetMapping("/api/registrations/events/{eventId}/users")
    List<RegistrationDto> getRegisteredUsers(@PathVariable("eventId") Long eventId);*/


    @GetMapping("/Internal/{eventId}/registeredUsers")
    List<Long> getRegisteredUsersIds(@PathVariable("eventId") Long eventId );

    
    
    record RegistrationDto(Long userId, Long eventId) {}

    
}