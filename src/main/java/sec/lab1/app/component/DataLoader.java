package sec.lab1.app.component;


import lombok.RequiredArgsConstructor;
import sec.lab1.app.model.LabUser;
import sec.lab1.app.repository.LabUserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final LabUserRepository users;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        users.findByUsername("alice").orElseGet(() ->
            users.save(LabUser.builder()
                .username("alice")
                .passwordHash(encoder.encode("password123"))
                .build())
        );
    }
}
