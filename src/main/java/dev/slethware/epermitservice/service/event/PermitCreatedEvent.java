package dev.slethware.epermitservice.service.event;

import dev.slethware.epermitservice.model.enums.PermitType;

import java.time.LocalDateTime;
import java.util.UUID;

public record PermitCreatedEvent(
        UUID permitId,
        String tenantId,
        String applicantEmail,
        PermitType permitType,
        Long amount,
        LocalDateTime timestamp
) {}