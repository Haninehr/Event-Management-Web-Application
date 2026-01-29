package com.eventmanagementisii.event.entity;

public enum EventStatus {
    ACTIVE,    // Default - event is live and accepting registrations
    ENDED,     // Will be shown only via getCurrentStatus() when date passed
    CANCELED   // Explicitly canceled by organizer
}