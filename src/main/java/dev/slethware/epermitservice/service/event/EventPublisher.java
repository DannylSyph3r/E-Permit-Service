package dev.slethware.epermitservice.service.event;

import dev.slethware.epermitservice.config.RabbitMQConfig;
import dev.slethware.epermitservice.model.entity.Permit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPermitCreated(Permit permit) {
        try {
            PermitCreatedEvent event = new PermitCreatedEvent(
                    permit.getId(),
                    permit.getTenantId(),
                    permit.getApplicantEmail(),
                    permit.getPermitType(),
                    permit.getAmount(),
                    LocalDateTime.now()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
            log.info("Published PermitCreatedEvent for permit: {}", permit.getId());
        } catch (Exception e) {
            // Best effort publishing, permit creation has already committed.
            // Log the failure and continue, do not rethrow.
            log.error("Failed to publish PermitCreatedEvent for permit {}: {}", permit.getId(), e.getMessage());
        }
    }
}