package org.nema.controller;

import org.nema.dto.AuthenticationRequest;
import org.nema.dto.AuthenticationResponse;
import org.nema.entities.AppUser;
import org.nema.entities.VerificationToken;
import org.nema.enums.UserStatus;
import org.nema.repository.VerificationTokenRepository;
import org.nema.security.AuthenticationService;
import org.nema.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v2/auth/users")
public class AppUserController {

    private final AppUserService appUserService;
    private final VerificationTokenRepository tokenRepository;
    private final AuthenticationService authenticationService;

    public AppUserController(final AppUserService appUserService,
                             VerificationTokenRepository tokenRepository,
                             AuthenticationService authenticationService) {
        this.appUserService = appUserService;
        this.tokenRepository = tokenRepository;
        this.authenticationService = authenticationService;
    }

    // Endpoint to create appUser
    @PostMapping(path = "/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> registerAppUser(@RequestBody AppUser appUser) {
        try {
            AppUser createAppUser = appUserService.registerAppUser(appUser);
            return ResponseEntity.ok(createAppUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Endpoint to verify user account
    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        try {
            VerificationToken verificationToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid token"));

            AppUser user = verificationToken.getAppUser();
            user.setStatus(UserStatus.ACTIVE);
            appUserService.saveUser(user);

            return ResponseEntity.ok("Account verified successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Verification failed: " + e.getMessage());
        }
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> logon(@RequestBody AuthenticationRequest authenticationRequest)  {
        AuthenticationResponse authenticationResponse = authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
        return ResponseEntity.ok(authenticationResponse);
    }

    // Endpoint for user logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        authenticationService.logout(jwtToken);
        return ResponseEntity.ok("Logged out successfully !");
    }

    // Read single user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id) {
        Optional<AppUser> user = appUserService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUser> getAllUsers() {
        return appUserService.getAllUsers();
    }

    // Update user by ID
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<AppUser> updateUser(@PathVariable Long id, @RequestBody AppUser updatedUser) {
        try {
            AppUser user = appUserService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete user by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
