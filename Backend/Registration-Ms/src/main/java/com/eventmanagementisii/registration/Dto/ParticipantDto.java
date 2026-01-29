//latest backend changes via grok : https://grok.com/c/6ae75cde-39c5-4dc4-b4c3-fec1b9585c28

package com.eventmanagementisii.registration.Dto;

import java.time.LocalDateTime;

public record ParticipantDto(
        Long userId,
        String username,
        String email,
        String status,
        LocalDateTime registeredAt
) {
    
}
