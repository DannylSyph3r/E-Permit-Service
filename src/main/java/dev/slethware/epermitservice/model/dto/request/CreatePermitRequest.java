package dev.slethware.epermitservice.model.dto.request;

import dev.slethware.epermitservice.model.enums.PermitType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePermitRequest(

        @NotBlank(message = "Applicant name is required")
        String applicantName,

        @NotBlank(message = "Applicant email is required")
        @Email(message = "Applicant email must be a valid email address")
        String applicantEmail,

        @NotNull(message = "Permit type is required")
        PermitType permitType,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Long amount
) {}