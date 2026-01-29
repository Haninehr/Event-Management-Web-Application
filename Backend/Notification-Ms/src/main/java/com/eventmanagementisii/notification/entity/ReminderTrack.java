package com.eventmanagementisii.notification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reminder_track")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private Long userId;

    private boolean sent;
}
