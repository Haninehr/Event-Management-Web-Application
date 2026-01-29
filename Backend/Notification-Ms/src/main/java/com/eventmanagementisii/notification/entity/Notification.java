package com.eventmanagementisii.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long eventid;
    private String title;
    private String message;

    private String type;  

    private LocalDateTime createdAt;

    @Builder.Default
    private boolean isread = false ;
}
