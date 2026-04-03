package dev.slethware.epermitservice.repository;

import dev.slethware.epermitservice.model.entity.Permit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PermitRepository extends JpaRepository<Permit, UUID> {

    // Loads all permits with their documents in one round trip.
    @Query("SELECT DISTINCT p FROM Permit p JOIN FETCH p.documents")
    List<Permit> findAllWithDocuments();
}