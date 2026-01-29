package com.eventmanagementisii.event.Dto;

import lombok.Data;
import java.time.LocalDate;
//import java.time.LocalDateTime;


import com.eventmanagementisii.event.entity.EventType;

//what can organizer update :
@Data
public class EventRequest {
    private String title;
    private String description;
    private LocalDate eventDate;
    private String eventTime;
    private String location;
    private EventType type;
   
    private Integer maxcapacity;
    
}

