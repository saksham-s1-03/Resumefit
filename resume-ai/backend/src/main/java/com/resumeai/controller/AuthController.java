package com.resumeai.controller;

import com.resumeai.model.AppUser;
import com.resumeai.repo.UserRepository;
import com.resumeai.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record Credentials(@Email @NotBlank String email, @NotBlank @Size(min = 8, max = 72) String password) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody Credentials c) {
        String email = c.email().trim().toLowerCase();
        if (users.findByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        AppUser u = users.save(new AppUser(email, encoder.encode(c.password())));
        return Map.of("token", jwt.generate(u.getId(), u.getEmail()), "email", u.getEmail());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody Credentials c) {
        AppUser u = users.findByEmail(c.email().trim().toLowerCase())
                .filter(x -> encoder.matches(c.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password."));
        return Map.of("token", jwt.generate(u.getId(), u.getEmail()), "email", u.getEmail());
    }
}
