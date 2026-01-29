package com.eventmanagementisii.registration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", url = "${app.notification-service-url}")
public interface NotificationClient {

    @PostMapping("/Internal/triggerRegistration")
    void notifyRegistration(@RequestBody Map<String, Long> payload);

    @PostMapping("/Internal/triggerRegistrationDecision")
    void notifyRegistrationDecision(@RequestBody Map<String, Object> payload);

    @PostMapping("/Internal/triggerUnregistration")
    void notifyUnRegistration(@RequestBody Map<String, Long> payload);


    
}