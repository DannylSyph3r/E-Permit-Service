package dev.slethware.epermitservice.service.permit;

import dev.slethware.epermitservice.model.dto.request.CreatePermitRequest;
import dev.slethware.epermitservice.model.dto.response.ApiResponse;
import dev.slethware.epermitservice.model.dto.response.PermitResponse;

import java.util.List;

public interface PermitService {
    PermitResponse createPermit(CreatePermitRequest request);
    ApiResponse<List<PermitResponse>> getPermitsSummary();
}