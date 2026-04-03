package dev.slethware.epermitservice.service.permit;

import dev.slethware.epermitservice.model.dto.request.CreatePermitRequest;
import dev.slethware.epermitservice.model.dto.response.ApiResponse;
import dev.slethware.epermitservice.model.dto.response.PermitResponse;
import dev.slethware.epermitservice.model.entity.Permit;
import dev.slethware.epermitservice.model.enums.PaymentStatus;
import dev.slethware.epermitservice.model.enums.PermitStatus;
import dev.slethware.epermitservice.repository.PermitRepository;
import dev.slethware.epermitservice.security.TenantContext;
import dev.slethware.epermitservice.service.event.EventPublisher;
import dev.slethware.epermitservice.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermitServiceImpl implements PermitService {

    private final PermitRepository permitRepository;
    private final PaymentService paymentService;
    private final EventPublisher eventPublisher;

    @Override
    public PermitResponse createPermit(CreatePermitRequest request) {
        String tenantId = TenantContext.getTenant();

        // Persist the application immediately, before payment is attempted.
        // The permit is recorded as PENDING regardless of payment outcome.
        Permit permit = Permit.builder()
                .tenantId(tenantId)
                .applicantName(request.applicantName())
                .applicantEmail(request.applicantEmail())
                .permitType(request.permitType())
                .amount(request.amount())
                .permitStatus(PermitStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        permitRepository.save(permit);
        log.info("Permit {} saved for tenant: {}", permit.getId(), tenantId);

        // Attempt payment, circuit breaker and retry handle transient failures.
        // The fallback in the payment service guarantees this never throws.
        String reference = "PERMIT-" + UUID.randomUUID();
        PaymentStatus paymentStatus = paymentService.chargePayment(permit.getAmount(), reference);
        permit.setPaymentStatus(paymentStatus);
        permitRepository.save(permit);
        log.info("Payment status for permit {}: {}", permit.getId(), paymentStatus);

        // Publish event (best-effort) failures are tolerated
        eventPublisher.publishPermitCreated(permit);

        return PermitResponse.from(permit);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PermitResponse>> getPermitsSummary() {
        List<PermitResponse> permits = permitRepository.findAllWithDocuments().stream()
                .map(PermitResponse::from)
                .toList();

        return ApiResponse.<List<PermitResponse>>builder()
                .status("success")
                .statusCode(200)
                .message("Permits retrieved successfully")
                .data(permits)
                .build();
    }
}