package org.nema.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}
