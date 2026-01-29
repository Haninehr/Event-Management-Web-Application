package com.eventmanagementisii.registration.repository;

import com.eventmanagementisii.registration.entity.Registration;
import com.eventmanagementisii.registration.entity.RegistrationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);
    int countByEventId(Long eventId);
    List<Registration> findByEventId(Long eventId);


    // In RegistrationRepository




List<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);

int countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    
}