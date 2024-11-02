package org.nema.security;

import lombok.RequiredArgsConstructor;
import org.nema.dto.AuthenticationResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtBlacklistService jwtBlacklistService;

    public AuthenticationResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtService.generateToken(email);

        return new AuthenticationResponse(token, null);
    }

    public void logout(String token) {
        jwtBlacklistService.blacklistToken(token);
        SecurityContextHolder.clearContext();
    }
}



