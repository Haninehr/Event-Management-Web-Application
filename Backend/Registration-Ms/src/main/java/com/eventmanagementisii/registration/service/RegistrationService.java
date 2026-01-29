package com.eventmanagementisii.registration.service;

import com.eventmanagementisii.registration.client.EventClient;
import com.eventmanagementisii.registration.client.NotificationClient;
import com.eventmanagementisii.registration.client.UserClient;
import com.eventmanagementisii.registration.entity.Registration;
import com.eventmanagementisii.registration.entity.RegistrationStatus;
import com.eventmanagementisii.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import com.eventmanagementisii.registration.Dto.ParticipantDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository repository;
    private final EventClient eventClient;
    private final NotificationClient notificationClient;
    private final UserClient userclient;

    @Transactional
    public String register(Long eventId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        boolean canceledbefore = false;
        Registration reg = null;
        // Check existing registration and its status
        var existing = repository.findByUserIdAndEventId(userId, eventId);
        if (existing.isPresent()) {
             reg = existing.get();
            if(reg.getStatus()==RegistrationStatus.CANCELED){
                canceledbefore = true;
            }else{
                 return switch (reg.getStatus()) {
                case ENATTEND -> "Votre demande de participation est en attente de confirmation.";
                case REGISTERED -> "Vous êtes déjà inscrit à cet événement !";
                case REFUSED -> "Votre demande a été refusée par l'organisateur.";
                case CANCELED -> "Vous aviez annulé votre inscription. Vous pouvez vous réinscrire.";
            };
            }
           
        }

        // Fetch event once
        EventClient.EventDto event = eventClient.getEvent(eventId);
        if (event == null) {
            return "Événement introuvable.";
        }

        if (event.status() != EventClient.EventStatus.ACTIVE) {
            return "Cet événement est annulé ou n'accepte plus d'inscriptions.";
        }

        if (LocalDate.now().isAfter(event.eventDate())) {
            return "Cet événement est déjà terminé.";
        }

        int currentRegistrations = repository.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
        if (currentRegistrations >= event.maxcapacity()) {
            return "L'événement est complet !";
        }

        if(!canceledbefore){
            
            // Create new pending registration
         reg = Registration.builder()
                .userId(userId)
                .eventId(eventId)
                .status(RegistrationStatus.ENATTEND)
                .build();

                
        }else{
            //if canceled before
            reg.setStatus(RegistrationStatus.ENATTEND);
        }

        
        repository.save(reg);
        

        

        // Notify organizer
        Map<String, Long> payload = Map.of("userId", userId, "eventId", eventId);
        notificationClient.notifyRegistration(payload);

        return "Demande de participation envoyée ! En attente de confirmation.";
    }

    @Transactional(readOnly = true)
    public String getRegistrationStatus(Long eventId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return repository.findByUserIdAndEventId(userId, eventId)
                .map(reg -> switch (reg.getStatus()) {
                    case ENATTEND -> "ENATTEND";
                    case REGISTERED -> "REGISTERED";
                    case REFUSED -> "REFUSED";
                    case CANCELED -> "CANCELED";
                })
                .orElse("Non inscrit");
    }


    //am i registred ? !!!
    @Transactional(readOnly = true)
    public boolean isRegistered(Long eventId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return repository.findByUserIdAndEventId(userId, eventId)
                .map(reg -> reg.getStatus() == RegistrationStatus.REGISTERED)
                .orElse(false);
    }


    //am i refsued ? !!!
    @Transactional(readOnly = true)
    public boolean isRefused(Long eventId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return repository.findByUserIdAndEventId(userId, eventId)
                .map(reg -> reg.getStatus() == RegistrationStatus.REFUSED)
                .orElse(false);
    }

    //am i enttend ? !!!
    @Transactional(readOnly = true)
    public boolean isEnattend(Long eventId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return repository.findByUserIdAndEventId(userId, eventId)
                .map(reg -> reg.getStatus() == RegistrationStatus.ENATTEND)
                .orElse(false);
    }

    

    @Transactional
    public String unregister(Long eventId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());

        return repository.findByUserIdAndEventId(userId, eventId)
                .map(reg -> {
                    if (reg.getStatus() == RegistrationStatus.CANCELED) {
                        return "Déjà annulé.";
                    }
                    reg.setStatus(RegistrationStatus.CANCELED);
                    repository.save(reg);

                    Map<String, Long> payload = Map.of("userId", userId, "eventId", eventId);
                    notificationClient.notifyUnRegistration(payload);

                    return "Inscription annulée avec succès.";
                })
                .orElse("Vous n'êtes pas inscrit à cet événement.");
    }

    // === Organizer actions ===

    @Transactional
    public String acceptParticipant(Long eventId, Long userId) {
        return updateParticipantStatus(eventId, userId, RegistrationStatus.REGISTERED, "accept");
    }

    @Transactional
    public String refuseParticipant(Long eventId, Long userId) {
        return updateParticipantStatus(eventId, userId, RegistrationStatus.REFUSED, "refuse");
    }

    private String updateParticipantStatus(Long eventId, Long userId, RegistrationStatus newStatus, String action) {
        return repository.findByUserIdAndEventId(userId, eventId)
                .map(reg -> {
                    if (reg.getStatus() == newStatus) {
                        return "Déjà " + (newStatus == RegistrationStatus.REGISTERED ? "accepté" : "refusé") + ".";
                    }

                    // Optional: prevent accepting if event is full
                    if (newStatus == RegistrationStatus.REGISTERED) {
                        int acceptedCount = repository.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
                        EventClient.EventDto event = eventClient.getEvent(eventId);
                        if (acceptedCount >= event.maxcapacity()) {
                            return "Impossible : l'événement est déjà complet.";
                        }
                    }

                    reg.setStatus(newStatus);
                    repository.save(reg);

                    Map<String, Object> payload = Map.of(
                        "userId", userId,
                        "eventId", eventId,
                        "accepted", newStatus == RegistrationStatus.REGISTERED
                    );
                    notificationClient.notifyRegistrationDecision(payload);

                    return "Participant " + (newStatus == RegistrationStatus.REGISTERED ? "accepté" : "refusé") + " avec succès.";
                })
                .orElse("Inscription introuvable.");
    }

    // === Queries for organizer dashboard ===


private List<ParticipantDto> getParticipantsByStatus(Long eventId, RegistrationStatus status) {
    // Common status string for the DTO (you can adjust if needed)
    String statusString = status.name(); // or switch to custom strings like "ENATTEND"

    List<Registration> regs = repository.findByEventIdAndStatus(eventId, status);

    if (regs.isEmpty()) {
        return List.of();
    }

    // Optional debug logging (you can remove later)
    regs.forEach(reg -> {
        System.out.println("reg.userId = " + reg.getUserId());
        System.out.println("reg.registeredAt = " + reg.getRegisteredAt());
        System.out.println("reg.status = " + reg.getStatus());
    });

    List<Long> userIds = regs.stream()
                            .map(Registration::getUserId)
                            .toList();

    Map<Long, UserClient.UserInfo> userMap = userclient.getUsersByIds(userIds);

    return regs.stream()
               .map(reg -> {
                   UserClient.UserInfo info = userMap.getOrDefault(
                       reg.getUserId(),
                       new UserClient.UserInfo("Deleted user", "deleted@deleted.com")
                   );

                   System.out.println("Création DTO pour userId = " + reg.getUserId() +
                                      " | date = " + reg.getRegisteredAt());

                   ParticipantDto pdto = new ParticipantDto(
                       reg.getUserId(),
                       info.username(),
                       info.email(),
                       statusString,  // Use the status we passed
                       reg.getRegisteredAt()
                   );

                   System.out.println("DTO créé → " + pdto);
                   return pdto;
               })
               .toList();
}

// Public methods — now very short and clear
@Transactional
public List<ParticipantDto> getPendingRegistrations(Long eventId) {
    return getParticipantsByStatus(eventId, RegistrationStatus.ENATTEND);
}
@Transactional
public List<ParticipantDto> getAcceptedParticipants(Long eventId) {
    return getParticipantsByStatus(eventId, RegistrationStatus.REGISTERED);
}
@Transactional
public List<ParticipantDto> getRefusedRegistrations(Long eventId) {
    return getParticipantsByStatus(eventId, RegistrationStatus.REFUSED);
}



    public int getParticipantCount(Long eventId) {
        return repository.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
    }

    public List<Long> getRegisteredUserIds(Long eventId) {
        return repository.findByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED)
                .stream()
                .map(Registration::getUserId)
                .toList();
    }


   
}