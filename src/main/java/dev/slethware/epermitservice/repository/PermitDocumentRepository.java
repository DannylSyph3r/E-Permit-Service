package dev.slethware.epermitservice.repository;

import dev.slethware.epermitservice.model.entity.PermitDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermitDocumentRepository extends JpaRepository<PermitDocument, UUID> {
}