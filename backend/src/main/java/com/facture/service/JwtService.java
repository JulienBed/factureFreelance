package com.facture.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "jwt.access.token.expiration", defaultValue = "900")
    long accessTokenExpiration;

    @ConfigProperty(name = "jwt.refresh.token.expiration", defaultValue = "604800")
    long refreshTokenExpiration;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateAccessToken(Long userId, String email) {
        Set<String> groups = new HashSet<>();
        groups.add("User");

        return Jwt.issuer(issuer)
                .upn(email)
                .subject(userId.toString())
                .groups(groups)
                .expiresAt(Instant.now().plusSeconds(accessTokenExpiration))
                .sign();
    }

    public String generateRefreshToken(Long userId, String email) {
        Set<String> groups = new HashSet<>();
        groups.add("User");

        return Jwt.issuer(issuer)
                .upn(email)
                .subject(userId.toString())
                .groups(groups)
                .claim("refresh", true)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpiration))
                .sign();
    }
}
