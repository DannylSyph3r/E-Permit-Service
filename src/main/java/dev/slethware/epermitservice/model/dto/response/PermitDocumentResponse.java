package dev.slethware.epermitservice.model.dto.response;

import dev.slethware.epermitservice.model.entity.PermitDocument;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PermitDocumentResponse(
        UUID id,
        String documentType,
        String documentUrl,
        LocalDateTime createdAt
) {
    public static PermitDocumentResponse from(PermitDocument document) {
        return PermitDocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .documentUrl(document.getDocumentUrl())
                .createdAt(document.getCreatedAt())
                .build();
    }
}