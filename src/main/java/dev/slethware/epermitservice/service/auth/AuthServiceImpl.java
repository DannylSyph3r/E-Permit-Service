package dev.slethware.epermitservice.service.auth;

import dev.slethware.epermitservice.exception.BadRequestException;
import dev.slethware.epermitservice.exception.UnauthorizedException;
import dev.slethware.epermitservice.model.dto.request.LoginRequest;
import dev.slethware.epermitservice.model.dto.request.RegisterRequest;
import dev.slethware.epermitservice.model.dto.response.ApiResponse;
import dev.slethware.epermitservice.model.dto.response.AuthResponse;
import dev.slethware.epermitservice.model.entity.User;
import dev.slethware.epermitservice.repository.UserRepository;
import dev.slethware.epermitservice.security.TenantWhitelist;
import dev.slethware.epermitservice.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public ApiResponse<Void> register(RegisterRequest request) {
        if (!TenantWhitelist.isValid(request.tenantId())) {
            throw new BadRequestException("Invalid tenant ID: " + request.tenantId());
        }

        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setTenantId(request.tenantId());
        user.setEnabled(true);

        userRepository.save(user);
        log.info("Registered new user: {} for tenant: {}", user.getEmail(), user.getTenantId());

        return ApiResponse.<Void>builder()
                .status("success")
                .statusCode(201)
                .message("Registration successful")
                .build();
    }

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().toLowerCase(),
                            request.password()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String token = tokenService.generateAccessToken(authentication, user);

            log.info("User authenticated: {}", user.getEmail());

            return ApiResponse.<AuthResponse>builder()
                    .status("success")
                    .statusCode(200)
                    .message("Authentication successful")
                    .data(new AuthResponse(token))
                    .build();

        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}