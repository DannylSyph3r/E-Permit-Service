package dev.slethware.epermitservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permit_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class PermitDocument extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permit_id", nullable = false)
    private Permit permit;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;
}