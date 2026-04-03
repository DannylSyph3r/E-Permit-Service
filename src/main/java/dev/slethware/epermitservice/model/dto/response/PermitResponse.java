package dev.slethware.epermitservice.model.dto.response;

import dev.slethware.epermitservice.model.entity.Permit;
import dev.slethware.epermitservice.model.enums.PaymentStatus;
import dev.slethware.epermitservice.model.enums.PermitStatus;
import dev.slethware.epermitservice.model.enums.PermitType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record PermitResponse(
        UUID id,
        String tenantId,
        String applicantName,
        String applicantEmail,
        PermitType permitType,
        Long amount,
        PermitStatus permitStatus,
        PaymentStatus paymentStatus,
        List<PermitDocumentResponse> documents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PermitResponse from(Permit permit) {
        return PermitResponse.builder()
                .id(permit.getId())
                .tenantId(permit.getTenantId())
                .applicantName(permit.getApplicantName())
                .applicantEmail(permit.getApplicantEmail())
                .permitType(permit.getPermitType())
                .amount(permit.getAmount())
                .permitStatus(permit.getPermitStatus())
                .paymentStatus(permit.getPaymentStatus())
                .documents(permit.getDocuments().stream()
                        .map(PermitDocumentResponse::from)
                        .toList())
                .createdAt(permit.getCreatedAt())
                .updatedAt(permit.getUpdatedAt())
                .build();
    }
}