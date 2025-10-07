package sec.lab1.app.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import sec.lab1.app.repository.LabUserRepository;
import sec.lab1.app.service.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

record LoginRequest(@NotBlank String username, @NotBlank String password) {}
record TokenResponse(String token) {}

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final LabUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest req) {
        var user = users.findByUsername(req.username())
                .orElse(null);
        if (user == null || !encoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new TokenResponse(jwt.issue(user.getUsername())));
    }
}
