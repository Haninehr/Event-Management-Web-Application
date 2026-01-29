package com.eventmanagementisii.notification.service;

import com.eventmanagementisii.notification.client.EventClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Reminder {

    private final EventClient eventClient;
    private final NotificationService notificationService;

    // Run every hour 3600000
    @Scheduled(fixedRate = 3600000)
    public void checkUpcomingEvents() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String dateStr = tomorrow.toString();
        
        try {
            List<EventClient.EventDto> events = eventClient.getUpcomingEvents(dateStr);

            for (var event : events) {
                log.info("Sending 24h reminder for event {}: {}", event.id(), event.title());
                notificationService.send24hReminder(event.id()); 
            }
        } catch (Exception e) {
            log.error("Error checking 24h reminders", e);
        }
    }
}