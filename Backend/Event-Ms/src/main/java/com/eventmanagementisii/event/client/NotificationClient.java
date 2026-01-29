package com.eventmanagementisii.event.client;

import java.util.Map;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${app.notification-sevice-id}", url = "${app.notification-service-url}")
public interface NotificationClient {

    @PostMapping("/Internal/triggerEventUpdate")
    void EventUpdateNotify(@RequestBody Map<String, Long> payload);

    @PostMapping("/Internal/triggerEventCancel")
    void EventCancelNotify(@RequestBody Map<String, Long> payload );
}