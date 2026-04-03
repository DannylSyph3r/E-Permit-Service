package dev.slethware.epermitservice.model.entity;

import dev.slethware.epermitservice.model.enums.PaymentStatus;
import dev.slethware.epermitservice.model.enums.PermitStatus;
import dev.slethware.epermitservice.model.enums.PermitType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class Permit extends Auditable {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "permit_type", nullable = false, length = 50)
    private PermitType permitType;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "permit_status", nullable = false, length = 20)
    private PermitStatus permitStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Builder.Default
    @OneToMany(mappedBy = "permit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PermitDocument> documents = new ArrayList<>();
}