package dev.slethware.epermitservice.controller;

import dev.slethware.epermitservice.model.dto.request.CreatePermitRequest;
import dev.slethware.epermitservice.model.dto.response.ApiResponse;
import dev.slethware.epermitservice.model.dto.response.PermitResponse;
import dev.slethware.epermitservice.service.permit.PermitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permits")
@RequiredArgsConstructor
@Tag(name = "Permits", description = "Endpoints for permit applications and retrieval")
public class PermitController {

    private final PermitService permitService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Submit a permit application",
            description = "Creates a permit for the requesting tenant, triggers a synchronous payment charge, and publishes a PermitCreated event."
    )
    public ResponseEntity<ApiResponse<PermitResponse>> createPermit(
            @Valid @RequestBody CreatePermitRequest request) {

        PermitResponse permit = permitService.createPermit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<PermitResponse>builder()
                        .status("success")
                        .statusCode(201)
                        .message("Permit application submitted successfully")
                        .data(permit)
                        .build()
        );
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get all permits for the requesting tenant",
            description = "Returns all permits with embedded documents. Uses JOIN FETCH — single query, no N+1."
    )
    public ResponseEntity<ApiResponse<List<PermitResponse>>> getPermitsSummary() {
        return ResponseEntity.ok(permitService.getPermitsSummary());
    }
}