package dev.slethware.epermitservice.service.permit;

import dev.slethware.epermitservice.model.dto.request.CreatePermitRequest;
import dev.slethware.epermitservice.model.dto.response.ApiResponse;
import dev.slethware.epermitservice.model.dto.response.PermitResponse;
import dev.slethware.epermitservice.model.entity.Permit;
import dev.slethware.epermitservice.model.entity.PermitDocument;
import dev.slethware.epermitservice.model.enums.PaymentStatus;
import dev.slethware.epermitservice.model.enums.PermitStatus;
import dev.slethware.epermitservice.repository.PermitDocumentRepository;
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

    private static final String DOCUMENT_URL_PREFIX = "https://docs.epermit.gov/documents/";

    private final PermitRepository permitRepository;
    private final PermitDocumentRepository permitDocumentRepository;
    private final PaymentService paymentService;
    private final EventPublisher eventPublisher;

    @Override
    public PermitResponse createPermit(CreatePermitRequest request) {
        String tenantId = TenantContext.getTenant();

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

        PermitDocument document = PermitDocument.builder()
                .permit(permit)
                .documentType("APPLICATION_FORM")
                .documentUrl(DOCUMENT_URL_PREFIX + UUID.randomUUID())
                .build();
        permitDocumentRepository.save(document);
        permit.getDocuments().add(document);

        String reference = "PERMIT-" + UUID.randomUUID();
        PaymentStatus paymentStatus = paymentService.chargePayment(permit.getAmount(), reference);
        permit.setPaymentStatus(paymentStatus);
        permitRepository.save(permit);
        log.info("Payment status for permit {}: {}", permit.getId(), paymentStatus);

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