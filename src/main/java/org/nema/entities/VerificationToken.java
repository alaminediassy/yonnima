package org.nema.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    private LocalDateTime expiryDate;

    // Default constructor
    public VerificationToken() {
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }

    // Constructor that accepts an AppUser
    public VerificationToken(AppUser appUser) {
        this.appUser = appUser;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }

    // Additional constructor if you want to specify a custom token and expiry date
    public VerificationToken(AppUser appUser, String token, LocalDateTime expiryDate) {
        this.appUser = appUser;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
