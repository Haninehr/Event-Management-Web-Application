package com.eventmanagementisii.registration.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "event_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; //this refer to participant and not the organizer 

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    //@Column(nullable = false)
    @Builder.Default  
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();


    @Builder.Default
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.ENATTEND; 
}