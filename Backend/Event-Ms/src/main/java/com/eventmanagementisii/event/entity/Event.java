package com.eventmanagementisii.event.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor  //generate (empty constructor) liek public Event(){}
@AllArgsConstructor //Lombok will create one constructor that takes one parameter for every field in the class
                    //so that we can pass the arguments directly without setter !
@Builder
@Setter
@Getter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private String eventTime; // or better: LocalTime

    @Column(nullable = false)
    private String location;

    @Column(nullable  = false)
    //@Enumerated(EnumType.STRING)
    private EventType type; //CONCERT, FORMARION , ....

    private Long organizerId;
    @Builder.Default
    private int views = 0;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventMedia> media = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    private Integer maxCapacity;

    // ====================== EVENT STATUS ======================

    //@Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.ACTIVE;

    // Helper method - very useful for APIs, frontend, queries
    @Transient
    public EventStatus getCurrentStatus() {
        if (status == EventStatus.CANCELED) {
            return EventStatus.CANCELED;
        }
        if (LocalDate.now().isAfter(eventDate)) {
            return EventStatus.ENDED;
        }
        return EventStatus.ACTIVE;
    }

    @Transient
    public boolean isActive() {
        return getCurrentStatus() == EventStatus.ACTIVE;
    }

    @Transient
    public boolean isEnded() {
        return getCurrentStatus() == EventStatus.ENDED;
    }

    @Transient
    public boolean isCanceled() {
        return status == EventStatus.CANCELED;
    }
}