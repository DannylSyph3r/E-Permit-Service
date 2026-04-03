package dev.slethware.epermitservice.service.payment;

import dev.slethware.epermitservice.model.enums.PaymentStatus;

public interface PaymentService {
    PaymentStatus chargePayment(Long amount, String reference);
}