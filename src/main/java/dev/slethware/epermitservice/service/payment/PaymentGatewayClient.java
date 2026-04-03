package dev.slethware.epermitservice.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentGatewayClient {

    private final WebClient webClient;
    private final String chargeUrl;

    public PaymentGatewayClient(WebClient webClient, @Value("${server.port:8080}") int port) {
        this.webClient = webClient;
        this.chargeUrl = "http://localhost:" + port + "/internal/payment/charge";
    }

    // Calls the payment gateway. Returns normally on success (2xx).
    // Throws RuntimeException on any error response so Resilience4j can intercept.
    public void charge(Long amount, String reference) {
        log.debug("Calling payment gateway for reference: {}", reference);
        webClient.post()
                .uri(chargeUrl)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    log.warn("Payment gateway error — status: {}", clientResponse.statusCode());
                    return Mono.error(new RuntimeException(
                            "Payment gateway returned: " + clientResponse.statusCode()));
                })
                .toBodilessEntity()
                .block();
    }
}