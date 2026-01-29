package com.eventmanagementisii.notification.service;

import com.eventmanagementisii.notification.client.EventClient;
import com.eventmanagementisii.notification.client.RegistrationClient;
import com.eventmanagementisii.notification.entity.Notification;
import com.eventmanagementisii.notification.entity.ReminderTrack;
import com.eventmanagementisii.notification.repository.NotificationRepository;
import com.eventmanagementisii.notification.repository.ReminderTrackRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final ReminderTrackRepository reminderTrackingRepository;
    private final EventClient eventClient;
    private final RegistrationClient registrationClient;

  

    //send notification 
    @SuppressWarnings("null")
    public void sendNotification(Long userId, Long eventId, String title, String message, String type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .eventid(eventId)
                .title(title)
                .message(message)
                .type(type)
                .createdAt(LocalDateTime.now())
                .isread(false)
                .build();
        repository.save(notification);
        
    }

    
    public void onReg(Long userId, Long eventId) {
        var event = eventClient.getEvent(eventId);
        String message = "Votre demande d'inscription à l'événement \"" + event.title() + "\" le " + event.eventDate();

        // Notify participant
        sendNotification(userId, eventId,
                "Demande de participation envoyée",
                message,
                "REGISTRATION_demande");

        // Notify organizer
        Long organizerId = eventClient.getOrganizerId(eventId);
        sendNotification(organizerId, eventId,
                "Participant inscrit",
                "Un participant a demandé à s'inscrire à votre événement \"" + event.title() + "\"",
                "REGISTRATION_ORG_demande");
    }


   
    public void onRegAccepted(Long userId, Long eventId) {
        var event = eventClient.getEvent(eventId);
        sendNotification(userId, eventId,
                "Inscription acceptée !",
                "Félicitations ! Votre demande pour l'événement \"" + event.title() + "\" a été acceptée.",
                "REGISTRATION_accept");
    }

 
    public void onRegRefused(Long userId, Long eventId) {
        var event = eventClient.getEvent(eventId);
        sendNotification(userId, eventId,
                "Inscription refusée",
                "Désolé, votre demande pour l'événement \"" + event.title() + "\" a été refusée par l'organisateur.",
                "REGISTRATION_Refuse");
    }

  
    public void onUnreg(Long userId, Long eventId) {
        var event = eventClient.getEvent(eventId);

        // Notify participant
        sendNotification(userId, eventId,
                "Désinscription confirmée",
                "Vous êtes désinscrit de l'événement \"" + event.title() + "\".",
                "UNREGISTRATION");

        // Notify organizer
        Long organizerId = eventClient.getOrganizerId(eventId);
        sendNotification(organizerId, eventId,
                "Participant désinscrit",
                "Un participant s'est désinscrit de votre événement \"" + event.title() + "\".",
                "UNREGISTRATION_ORG");
    }


    public void onUpdate(Long eventId) {
        var event = eventClient.getEvent(eventId);
        List<Long> participants = registrationClient.getRegisteredUsersIds(eventId);

        
        for (Long userId : participants) {
            sendNotification(userId, eventId,
                    "Événement mis à jour",
                    "L'événement \"" + event.title() + "\" a été modifié.",
                    "UPDATE");
        }
    }


    public void onCancel(Long eventId) {
        var event = eventClient.getEvent(eventId);
        List<Long> participants = registrationClient.getRegisteredUsersIds(eventId);

        for (Long userId : participants) {
            sendNotification(userId, eventId,
                    "Événement annulé",
                    "L'événement \"" + event.title() + "\" a été annulé.",
                    "CANCEL");
        }
    }

    // 24h Reminder
 
    @SuppressWarnings("null")
    public void send24hReminder(Long eventId) {
        var event = eventClient.getEvent(eventId);
        List<Long> participants = registrationClient.getRegisteredUsersIds(eventId);

        for (Long userId : participants) {

            // Check if reminder already sent
            boolean alreadySent = reminderTrackingRepository
                    .findByEventIdAndUserId(eventId, userId)
                    .map(ReminderTrack::isSent)
                    .orElse(false);

            if (!alreadySent) {
                sendNotification(userId, eventId,
                        "Rappel : Événement demain !",
                        "L'événement \"" + event.title() + "\" aura lieu demain à " +
                                event.eventTime() + " à " + event.location(),
                        "REMINDER");

                // Mark as sent
                ReminderTrack tracking = ReminderTrack.builder()
                        .eventId(eventId)
                        .userId(userId)
                        .sent(true)
                        .build();
                reminderTrackingRepository.save(tracking);
            }
        }
    }

    
    public List<Notification> getNotificationsForUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }


    //masrk as read !
    @SuppressWarnings("null")
    public void markAsRead(Long notificationId) {
        repository.findById(notificationId).ifPresent(n -> {
            n.setIsread(true);
            repository.save(n);
        });
    }
}
