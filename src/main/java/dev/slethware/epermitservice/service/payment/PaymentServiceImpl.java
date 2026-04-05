package dev.slethware.epermitservice.service.payment;

import dev.slethware.epermitservice.model.enums.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String INSTANCE = "paymentGateway";

    private final PaymentGatewayClient gatewayClient;

    @Override
    @Retry(name = INSTANCE, fallbackMethod = "chargePaymentFallback")
    @CircuitBreaker(name = INSTANCE, fallbackMethod = "chargePaymentFallback")
    public PaymentStatus chargePayment(Long amount, String reference) {
        gatewayClient.charge(amount, reference);
        log.info("Payment succeeded for reference: {}", reference);
        return PaymentStatus.SUCCESS;
    }

    // Never throws — returns FAILED so permit creation can complete gracefully.
    private PaymentStatus chargePaymentFallback(Long amount, String reference, Throwable cause) {
        log.warn("Payment fallback triggered for reference: {}. Cause: {}", reference, cause.getMessage());
        return PaymentStatus.FAILED;
    }
}