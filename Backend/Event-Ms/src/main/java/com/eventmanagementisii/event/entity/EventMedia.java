package com.eventmanagementisii.event.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String mediaType; // IMAGE, VIDEO, DOCUMENT

    @Column
    private String title;
}