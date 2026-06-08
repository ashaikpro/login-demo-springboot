package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.Emp;
import com.example.demo.repository.EmpRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class AuthService {

    private final EmpRepository empRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Key jwtKey;
    private final long jwtExpiryMinutes;

    public AuthService(EmpRepository empRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       @Value("${jwt.secret}") String jwtSecret,
                       @Value("${jwt.expiration-minutes:15}") long jwtExpiryMinutes) {
        this.empRepository = empRepository;
        this.passwordEncoder = passwordEncoder;
        // secret should be strong and at least 32 bytes for HS256
        this.jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiryMinutes = jwtExpiryMinutes;
    }

    /**
     * Authenticate user, return JWT on success or null on failure.
     * Uses BCrypt to verify password; assumes stored password is BCrypt-hashed.
     */
    public String authenticate(LoginRequest request) {
        Emp emp = empRepository.findByUsername(request.getUsername()).orElse(null);
        // Always use a constant-time check via BCrypt#matches to avoid timing leaks (BCrypt handles it)
        if (emp == null) {
            return null;
        }

        if (!passwordEncoder.matches(request.getPassword(), emp.getPassword())) {
            return null;
        }

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiry = Date.from(now.plus(jwtExpiryMinutes, ChronoUnit.MINUTES));

        return Jwts.builder()
                .setSubject(emp.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiry)
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

