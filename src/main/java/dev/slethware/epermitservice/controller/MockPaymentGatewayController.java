package dev.slethware.epermitservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// Simulates an external payment gateway for local testing only.
// Sleeps 3 seconds per call to mimic network latency, then returns 503 or 200 with a payment reference
@Slf4j
@Hidden
@RestController
@RequestMapping("/internal/payment")
public class MockPaymentGatewayController {

    @PostMapping("/charge")
    public ResponseEntity<Map<String, String>> charge() throws InterruptedException {
        Thread.sleep(3_000);

        if (ThreadLocalRandom.current().nextDouble() < 0.30) {
            log.debug("Mock gateway: simulating 503 failure");
            return ResponseEntity.status(503).build();
        }

        String reference = "PAY-" + UUID.randomUUID();
        log.debug("Mock gateway: success, reference={}", reference);
        return ResponseEntity.ok(Map.of("reference", reference));
    }
}