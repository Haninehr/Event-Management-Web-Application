package com.eventmanagementisii.event.Dto;

import lombok.Data;
import java.time.LocalDate;
//import java.time.LocalDateTime;
import java.util.List;

import com.eventmanagementisii.event.entity.EventStatus;
import com.eventmanagementisii.event.entity.EventType;
//import com.eventmanagementisii.event.entity.EventStatus;

@Data
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate eventDate;
    private String eventTime;
    private String location;
    private EventType type;
    private Long organizerId;
    private int views;
    private List<MediaDto> medias;
    private LocalDate createdAt;
    private Integer maxcapacity;
    private EventStatus status;
}